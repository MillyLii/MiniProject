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

import java.time.LocalDate;
import java.util.List;

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

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelBooking(@PathVariable Long id) {
        if (!bookingRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Booking not found"
            );
        }

        bookingRepository.deleteById(id);
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
