package com.deskflow.controller;

import com.deskflow.dto.ApiDtos.DeskResponse;
import com.deskflow.service.DeskBookingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/desks")
public class DeskController {

    private final DeskBookingService deskBookingService;

    public DeskController(DeskBookingService deskBookingService) {
        this.deskBookingService = deskBookingService;
    }

    @GetMapping
    public List<DeskResponse> listDesks(
            @RequestParam(required = false) Integer floor,
            @RequestParam(required = false) Boolean hasMonitor
    ) {
        return deskBookingService.listDesks(floor, hasMonitor);
    }

    /** Creative feature: which active desks are free on date X? */
    @GetMapping("/available")
    public List<DeskResponse> listAvailable(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return deskBookingService.listAvailableDesks(date);
    }
}
