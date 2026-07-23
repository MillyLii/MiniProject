package com.deskflow.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record BookingResponse(
        Long id,
        Long deskId,
        String deskCode,
        String employeeName,
        LocalDate date,
        LocalDateTime createdAt
) {
}
