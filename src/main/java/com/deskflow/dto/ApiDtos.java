package com.deskflow.dto;

import com.deskflow.model.Booking;
import com.deskflow.model.Desk;

import java.time.Instant;
import java.time.LocalDate;

public final class ApiDtos {

    private ApiDtos() {
    }

    public record DeskResponse(
            Long id,
            String code,
            Integer floor,
            boolean hasMonitor,
            boolean active
    ) {
        public static DeskResponse from(Desk desk) {
            return new DeskResponse(
                    desk.getId(),
                    desk.getCode(),
                    desk.getFloor(),
                    desk.isHasMonitor(),
                    desk.isActive()
            );
        }
    }

    public record CreateBookingRequest(
            Long deskId,
            String employeeName,
            LocalDate date
    ) {
    }

    public record BookingResponse(
            Long id,
            Long deskId,
            String deskCode,
            String employeeName,
            LocalDate date,
            Instant createdAt
    ) {
        public static BookingResponse from(Booking booking) {
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

    public record ErrorResponse(String error, String details) {
    }

    public record HealthResponse(String status) {
    }
}
