package com.deskflow.repository;

import com.deskflow.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByDate(LocalDate date);

    Optional<Booking> findByDeskIdAndDate(Long deskId, LocalDate date);

    List<Booking> findByEmployeeNameIgnoreCase(String employeeName);
}
