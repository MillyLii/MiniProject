package com.deskflow.controller;

import com.deskflow.model.Desk;
import com.deskflow.repository.DeskRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/desks")
public class DeskController {

    private final DeskRepository deskRepository;

    public DeskController(DeskRepository deskRepository) {
        this.deskRepository = deskRepository;
    }

    @GetMapping
    public List<Desk> getAllDesks(
            @RequestParam(required = false) Integer floor,
            @RequestParam(required = false) Boolean hasMonitor
    ) {
        if (floor != null && hasMonitor != null) {
            return deskRepository.findByFloorAndHasMonitor(
                    floor,
                    hasMonitor
            );
        }

        if (floor != null) {
            return deskRepository.findByFloor(floor);
        }

        if (hasMonitor != null) {
            return deskRepository.findByHasMonitor(hasMonitor);
        }

        return deskRepository.findAll();
    }

    @GetMapping("/available")
    public List<Desk> getAvailableDesks(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,

            @RequestParam(required = false)
            Integer floor,

            @RequestParam(required = false)
            Boolean hasMonitor
    ) {
        LocalDate today = LocalDate.now();
        LocalDate latest = today.plusDays(6);
        if (date.isBefore(today) || date.isAfter(latest)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Availability can only be queried from today through the next 7 days ("
                            + today + " to " + latest + ")"
            );
        }

        return deskRepository.findAvailableDesks(
                date,
                floor,
                hasMonitor
        );
    }
}
