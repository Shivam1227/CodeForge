package com.judge.api.config;

import com.judge.api.entity.Role;
import com.judge.api.entity.User;
import com.judge.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (userRepository.existsByUsername(adminUsername)) {
            return;
        }

        User admin = User.builder()
                .username(adminUsername)
                .email(adminEmail)
                .passwordHash(passwordEncoder.encode(adminPassword))
                .role(Role.ADMIN)
                .build();

        userRepository.save(admin);

        System.out.println("Default admin user created: " + adminUsername);
    }
}