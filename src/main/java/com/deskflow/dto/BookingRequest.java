package com.deskflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record BookingRequest(
        @NotNull Long deskId,
        @NotBlank String employeeName,
        @NotNull LocalDate date
) {
}
