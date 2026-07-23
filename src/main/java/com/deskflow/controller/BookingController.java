package com.deskflow.controller;

import com.deskflow.dto.BookingRequest;
import com.deskflow.dto.BookingResponse;
import com.deskflow.model.Booking;
import com.deskflow.model.Desk;
import com.deskflow.repository.BookingRepository;
import com.deskflow.repository.DeskRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingRepository bookingRepository;
    private final DeskRepository deskRepository;

    public BookingController(
            BookingRepository bookingRepository,
            DeskRepository deskRepository
    ) {
        this.bookingRepository = bookingRepository;
        this.deskRepository = deskRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse createBooking(
            @Valid @RequestBody BookingRequest request
    ) {
        Desk desk = deskRepository.findById(request.deskId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Desk not found"
                ));

        if (!desk.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Inactive desk cannot be booked"
            );
        }

        if (request.employeeName().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Employee name cannot be blank"
            );
        }

        if (bookingRepository.existsByDeskIdAndDate(
                request.deskId(),
                request.date()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Desk is already booked for this date"
            );
        }

        Booking booking = new Booking();
        booking.setDesk(desk);
        booking.setEmployeeName(request.employeeName().trim());
        booking.setDate(request.date());

        Booking saved = bookingRepository.save(booking);

        return toResponse(saved);
    }

    @GetMapping
    public List<BookingResponse> getBookingsByDate(
            @RequestParam LocalDate date
    ) {
        return bookingRepository.findByDate(date)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Current calendar week (Monday–Sunday) bookings for one employee.
     */
    @GetMapping("/week")
    public Map<String, Object> getMyWeekBookings(
            @RequestParam String employeeName
    ) {
        if (employeeName == null || employeeName.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "employeeName is required"
            );
        }

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = today.with(DayOfWeek.SUNDAY);

        List<BookingResponse> bookings = bookingRepository
                .findByEmployeeNameIgnoreCaseAndDateBetweenOrderByDateAsc(
                        employeeName.trim(),
                        weekStart,
                        weekEnd
                )
                .stream()
                .map(this::toResponse)
                .toList();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("employeeName", employeeName.trim());
        body.put("weekStart", weekStart);
        body.put("weekEnd", weekEnd);
        body.put("bookings", bookings);
        return body;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelBooking(
            @PathVariable Long id,
            @RequestParam String employeeName
    ) {
        if (employeeName == null || employeeName.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "employeeName is required to cancel a booking"
            );
        }

        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Booking not found"
                ));

        if (!booking.getEmployeeName().equalsIgnoreCase(employeeName.trim())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only cancel your own bookings"
            );
        }

        bookingRepository.delete(booking);
    }

    private BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getDesk().getId(),
                booking.getDesk().getCode(),
                booking.getEmployeeName(),
                booking.getDate(),
                booking.getCreatedAt()
        );
    }
}
