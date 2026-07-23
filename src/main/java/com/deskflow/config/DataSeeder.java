package com.deskflow.config;

import com.deskflow.model.Booking;
import com.deskflow.model.Desk;
import com.deskflow.repository.BookingRepository;
import com.deskflow.repository.DeskRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedDatabase(
            DeskRepository deskRepository,
            BookingRepository bookingRepository
    ) {
        return args -> {
            if (deskRepository.count() > 0) {
                return;
            }

            Desk desk1 = new Desk(
                null,
                "HEL-2F-01",
                2,
                true,
                true
            );

            Desk desk2 = new Desk(
                null,
                "HEL-2F-02",
                2,
                true,
                true
            );

            Desk desk3 = new Desk(
                null,
                "HEL-2F-03",
                2,
                false,
                true
            );

            Desk desk4 = new Desk(
                null,
                "HEL-2F-04",
                2,
                false,
                false
            );

            Desk desk5 = new Desk(
                null,
                "HEL-3F-01",
                3,
                true,
                true
            );

            Desk desk6 = new Desk(
                null,
                "HEL-3F-02",
                3,
                true,
                true
            );

            Desk desk7 = new Desk(
                null,
                "HEL-3F-03",
                3,
                false,
                true
            );

            Desk desk8 = new Desk(
                null,
                "HEL-3F-04",
                3,
                false,
                true
            );

            deskRepository.save(desk1);
            deskRepository.save(desk2);
            deskRepository.save(desk3);
            deskRepository.save(desk4);
            deskRepository.save(desk5);
            deskRepository.save(desk6);
            deskRepository.save(desk7);
            deskRepository.save(desk8);

            Booking booking1 = new Booking();
            booking1.setDesk(desk1);
            booking1.setEmployeeName("Anna Kowalska");
            booking1.setDate(LocalDate.of(2026, 7, 24));

            Booking booking2 = new Booking();
            booking2.setDesk(desk5);
            booking2.setEmployeeName("Mikko Korhonen");
            booking2.setDate(LocalDate.of(2026, 7, 24));

            Booking booking3 = new Booking();
            booking3.setDesk(desk2);
            booking3.setEmployeeName("Laura Nieminen");
            booking3.setDate(LocalDate.of(2026, 7, 25));

            bookingRepository.save(booking1);
            bookingRepository.save(booking2);
            bookingRepository.save(booking3);

            System.out.println("DeskFlow database seeded successfully.");
        };
    }
}