package com.deskflow.config;

import com.deskflow.model.Booking;
import com.deskflow.model.Desk;
import com.deskflow.repository.BookingRepository;
import com.deskflow.repository.DeskRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final DeskRepository deskRepository;
    private final BookingRepository bookingRepository;

    public DataSeeder(DeskRepository deskRepository, BookingRepository bookingRepository) {
        this.deskRepository = deskRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public void run(String... args) {
        if (deskRepository.count() > 0) {
            return;
        }

        List<Desk> desks = deskRepository.saveAll(List.of(
                new Desk("HEL-3F-01", 3, true, true),
                new Desk("HEL-3F-02", 3, true, true),
                new Desk("HEL-3F-03", 3, false, true),
                new Desk("HEL-3F-04", 3, true, false),
                new Desk("HEL-4F-01", 4, true, true),
                new Desk("HEL-4F-02", 4, false, true),
                new Desk("HEL-4F-03", 4, true, true),
                new Desk("HEL-5F-01", 5, true, true),
                new Desk("HEL-5F-02", 5, false, true)
        ));

        LocalDate today = LocalDate.of(2026, 7, 24);
        bookingRepository.saveAll(List.of(
                new Booking(desks.get(0), "Anna Kowalska", today),
                new Booking(desks.get(4), "Mikko Virtanen", today),
                new Booking(desks.get(7), "Liisa Nieminen", today.plusDays(1))
        ));
    }
}
