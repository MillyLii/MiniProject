package com.deskflow.controller;

import com.deskflow.dto.ApiDtos.BookingResponse;
import com.deskflow.dto.ApiDtos.CreateBookingRequest;
import com.deskflow.service.DeskBookingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final DeskBookingService deskBookingService;

    public BookingController(DeskBookingService deskBookingService) {
        this.deskBookingService = deskBookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> create(@RequestBody CreateBookingRequest request) {
        BookingResponse created = deskBookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public List<BookingResponse> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String employeeName
    ) {
        if (employeeName != null && !employeeName.isBlank()) {
            return deskBookingService.listBookingsByEmployee(employeeName);
        }
        return deskBookingService.listBookingsByDate(date);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        deskBookingService.cancelBooking(id);
        return ResponseEntity.noContent().build();
    }
}
