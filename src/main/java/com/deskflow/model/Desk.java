package com.deskflow.model;

import jakarta.persistence.*;

@Entity
@Table(
    name = "desks",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_desk_code",
            columnNames = "code"
        )
    }
)
public class Desk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false)
    private Integer floor;

    @Column(name = "has_monitor", nullable = false)
    private boolean hasMonitor;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    public Desk() {
    }

    public Desk(
            Long id,
            String code,
            Integer floor,
            boolean hasMonitor,
            boolean active
    ) {
        this.id = id;
        this.code = code;
        this.floor = floor;
        this.hasMonitor = hasMonitor;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

    public boolean isHasMonitor() {
        return hasMonitor;
    }

    public void setHasMonitor(boolean hasMonitor) {
        this.hasMonitor = hasMonitor;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}