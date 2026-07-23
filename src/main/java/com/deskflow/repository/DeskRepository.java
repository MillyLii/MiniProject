package com.deskflow.repository;

import com.deskflow.model.Desk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DeskRepository extends JpaRepository<Desk, Long> {

    Optional<Desk> findByCode(String code);

    List<Desk> findByFloor(Integer floor);

    List<Desk> findByHasMonitor(boolean hasMonitor);

    List<Desk> findByFloorAndHasMonitor(
            Integer floor,
            boolean hasMonitor
    );

    List<Desk> findByActiveTrue();

    @Query("""
        SELECT d
        FROM Desk d
        WHERE d.active = true
          AND (:floor IS NULL OR d.floor = :floor)
          AND (:hasMonitor IS NULL OR d.hasMonitor = :hasMonitor)
          AND NOT EXISTS (
              SELECT b
              FROM Booking b
              WHERE b.desk = d
                AND b.date = :date
          )
        ORDER BY d.floor, d.code
        """)
    List<Desk> findAvailableDesks(
            @Param("date") LocalDate date,
            @Param("floor") Integer floor,
            @Param("hasMonitor") Boolean hasMonitor
    );
}
