package com.deskflow.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "bookings",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_booking_desk_date",
            columnNames = {"desk_id", "booking_date"}
        )
    }
)
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "desk_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_booking_desk")
    )
    private Desk desk;

    @Column(name = "employee_name", nullable = false, length = 100)
    private String employeeName;

    @Column(name = "booking_date", nullable = false)
    private LocalDate date;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Booking() {
    }

    public Booking(
            Long id,
            Desk desk,
            String employeeName,
            LocalDate date,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.desk = desk;
        this.employeeName = employeeName;
        this.date = date;
        this.createdAt = createdAt;
    }

    @PrePersist
    public void setCreatedAtAutomatically() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Desk getDesk() {
        return desk;
    }

    public void setDesk(Desk desk) {
        this.desk = desk;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}