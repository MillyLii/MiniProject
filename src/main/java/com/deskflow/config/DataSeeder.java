package com.deskflow.config;

import com.deskflow.model.Booking;
import com.deskflow.model.Desk;
import com.deskflow.repository.BookingRepository;
import com.deskflow.repository.DeskRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.List;

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
                    true
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
                    false
            );

            Desk desk9 = new Desk(
                    null,
                    "HEL-4F-01",
                    4,
                    true,
                    true
            );

            Desk desk10 = new Desk(
                    null,
                    "HEL-4F-02",
                    4,
                    true,
                    true
            );

            Desk desk11 = new Desk(
                    null,
                    "HEL-4F-03",
                    4,
                    false,
                    true
            );

            Desk desk12 = new Desk(
                    null,
                    "HEL-4F-04",
                    4,
                    true,
                    true
            );

            deskRepository.saveAll(List.of(
                    desk1,
                    desk2,
                    desk3,
                    desk4,
                    desk5,
                    desk6,
                    desk7,
                    desk8,
                    desk9,
                    desk10,
                    desk11,
                    desk12
            ));

            bookingRepository.save(
                    createBooking(
                            desk1,
                            "Anna Kowalska",
                            LocalDate.of(2026, 7, 23)
                    )
            );

            bookingRepository.save(
                    createBooking(
                            desk5,
                            "Mikko Korhonen",
                            LocalDate.of(2026, 7, 23)
                    )
            );

            bookingRepository.save(
                    createBooking(
                            desk9,
                            "Laura Nieminen",
                            LocalDate.of(2026, 7, 23)
                    )
            );

            bookingRepository.save(
                    createBooking(
                            desk2,
                            "Sofia Virtanen",
                            LocalDate.of(2026, 7, 24)
                    )
            );

            bookingRepository.save(
                    createBooking(
                            desk6,
                            "Oliver Laine",
                            LocalDate.of(2026, 7, 24)
                    )
            );

            bookingRepository.save(
                    createBooking(
                            desk10,
                            "Emilia Heikkinen",
                            LocalDate.of(2026, 7, 24)
                    )
            );

            bookingRepository.save(
                    createBooking(
                            desk3,
                            "Lucas Niemi",
                            LocalDate.of(2026, 7, 25)
                    )
            );

            bookingRepository.save(
                    createBooking(
                            desk7,
                            "Ella Mäkinen",
                            LocalDate.of(2026, 7, 25)
                    )
            );

            bookingRepository.save(
                    createBooking(
                            desk11,
                            "Noah Salonen",
                            LocalDate.of(2026, 7, 25)
                    )
            );

            bookingRepository.save(
                    createBooking(
                            desk4,
                            "Aino Lehtinen",
                            LocalDate.of(2026, 7, 26)
                    )
            );

            bookingRepository.save(
                    createBooking(
                            desk12,
                            "Leo Hämäläinen",
                            LocalDate.of(2026, 7, 26)
                    )
            );

            System.out.println("=================================");
            System.out.println("DeskFlow database seeded");
            System.out.println("12 desks created");
            System.out.println("11 bookings created");
            System.out.println("=================================");
        };
    }

    private Booking createBooking(
            Desk desk,
            String employeeName,
            LocalDate date
    ) {
        Booking booking = new Booking();
        booking.setDesk(desk);
        booking.setEmployeeName(employeeName);
        booking.setDate(date);

        return booking;
    }
}
