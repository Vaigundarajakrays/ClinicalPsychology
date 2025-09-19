package com.clinicalpsychology.app.seed;

import com.clinicalpsychology.app.enums.Role;
import com.clinicalpsychology.app.model.Users;
import com.clinicalpsychology.app.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {

        String adminEmail = "admin@gmail.com";

        boolean exists = usersRepository.existsByEmailId(adminEmail);

        if(!exists){

            try {
                String password = "admin@123";
                String hashedPassword = passwordEncoder.encode(password);

                Users user = Users.builder()
                        .emailId(adminEmail)
                        .password(hashedPassword)
                        .role(Role.ADMIN)
                        .build();

                usersRepository.save(user);
                log.info("✅ Admin user seeded successfully.");
            } catch (Exception e) {
                log.error("❌ Failed to seed admin user", e);
            }

        } else {
            log.warn("⚠️ Admin with email {} already exists. Skipping seeding.", adminEmail);
        }



    }
}
