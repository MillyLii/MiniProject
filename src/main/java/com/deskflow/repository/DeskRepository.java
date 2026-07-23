package com.deskflow.repository;

import com.deskflow.model.Desk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeskRepository extends JpaRepository<Desk, Long> {

    List<Desk> findByFloor(Integer floor);

    List<Desk> findByHasMonitor(boolean hasMonitor);

    List<Desk> findByFloorAndHasMonitor(Integer floor, boolean hasMonitor);

    List<Desk> findByActiveTrue();
}
