package com.deskflow.repository;

import com.deskflow.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByDate(LocalDate date);

    boolean existsByDeskIdAndDate(Long deskId, LocalDate date);
}
