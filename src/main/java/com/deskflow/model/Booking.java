package com.deskflow.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
        name = "booking",
        uniqueConstraints = @UniqueConstraint(name = "uk_desk_date", columnNames = {"desk_id", "booking_date"})
)
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "desk_id", nullable = false)
    private Desk desk;

    @Column(name = "employee_name", nullable = false, length = 120)
    private String employeeName;

    @Column(name = "booking_date", nullable = false)
    private LocalDate date;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    protected Booking() {
    }

    public Booking(Desk desk, String employeeName, LocalDate date) {
        this.desk = desk;
        this.employeeName = employeeName;
        this.date = date;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Desk getDesk() {
        return desk;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public LocalDate getDate() {
        return date;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
