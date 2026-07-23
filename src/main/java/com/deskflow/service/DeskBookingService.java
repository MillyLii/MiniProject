package com.deskflow.service;

import com.deskflow.dto.ApiDtos.BookingResponse;
import com.deskflow.dto.ApiDtos.CreateBookingRequest;
import com.deskflow.dto.ApiDtos.DeskResponse;
import com.deskflow.exception.ApiException;
import com.deskflow.model.Booking;
import com.deskflow.model.Desk;
import com.deskflow.repository.BookingRepository;
import com.deskflow.repository.DeskRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class DeskBookingService {

    private final DeskRepository deskRepository;
    private final BookingRepository bookingRepository;

    public DeskBookingService(DeskRepository deskRepository, BookingRepository bookingRepository) {
        this.deskRepository = deskRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional(readOnly = true)
    public List<DeskResponse> listDesks(Integer floor, Boolean hasMonitor) {
        List<Desk> desks;
        if (floor != null && hasMonitor != null) {
            desks = deskRepository.findByFloorAndHasMonitor(floor, hasMonitor);
        } else if (floor != null) {
            desks = deskRepository.findByFloor(floor);
        } else if (hasMonitor != null) {
            desks = deskRepository.findByHasMonitor(hasMonitor);
        } else {
            desks = deskRepository.findAll();
        }
        return desks.stream().map(DeskResponse::from).toList();
    }

    /** Creative: desks that are active and not booked on the given date. */
    @Transactional(readOnly = true)
    public List<DeskResponse> listAvailableDesks(LocalDate date) {
        if (date == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid request", "date is required");
        }
        Set<Long> bookedDeskIds = new HashSet<>();
        for (Booking booking : bookingRepository.findByDate(date)) {
            bookedDeskIds.add(booking.getDesk().getId());
        }
        return deskRepository.findByActiveTrue().stream()
                .filter(desk -> !bookedDeskIds.contains(desk.getId()))
                .map(DeskResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> listBookingsByDate(LocalDate date) {
        if (date == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid request", "date query param is required");
        }
        return bookingRepository.findByDate(date).stream()
                .map(BookingResponse::from)
                .toList();
    }

    /** Creative helper: all bookings for one employee. */
    @Transactional(readOnly = true)
    public List<BookingResponse> listBookingsByEmployee(String employeeName) {
        if (employeeName == null || employeeName.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid request", "employeeName is required");
        }
        return bookingRepository.findByEmployeeNameIgnoreCase(employeeName.trim()).stream()
                .map(BookingResponse::from)
                .toList();
    }

    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {
        if (request == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid request", "body is required");
        }
        if (request.deskId() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid request", "deskId is required");
        }
        if (request.employeeName() == null || request.employeeName().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid request", "employeeName must not be blank");
        }
        if (request.date() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid request", "date is missing or invalid");
        }

        Desk desk = deskRepository.findById(request.deskId())
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Desk not found",
                        "No desk with id " + request.deskId()
                ));

        if (!desk.isActive()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Desk inactive",
                    "Desk " + desk.getCode() + " cannot be booked"
            );
        }

        if (bookingRepository.findByDeskIdAndDate(desk.getId(), request.date()).isPresent()) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Desk already booked",
                    "Desk " + desk.getCode() + " is already booked on " + request.date()
            );
        }

        Booking booking = bookingRepository.save(
                new Booking(desk, request.employeeName().trim(), request.date())
        );
        return BookingResponse.from(booking);
    }

    @Transactional
    public void cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Booking not found",
                        "No booking with id " + id
                ));
        bookingRepository.delete(booking);
    }
}
