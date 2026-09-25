package com.sponsorbox;

import com.sponsorbox.models.User;
import com.sponsorbox.models.UserRole;
import com.sponsorbox.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class SponsorBoxApplication {

    public static void main(String[] args) {
        SpringApplication.run(SponsorBoxApplication.class, args);
        System.out.println("=================================================");
        System.out.println("  SponsorBox Backend Spring Boot est demarre !");
        System.out.println("=================================================");
    }

    @Bean
    CommandLineRunner createAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByEmail("admin@sponsorbox.com").isEmpty()) {
                User admin = User.builder()
                        .email("admin@sponsorbox.com")
                        .password(passwordEncoder.encode("admin123"))
                        .role(UserRole.ADMIN)
                        .build();
                userRepository.save(admin);
                System.out.println("  Compte admin cree : admin@sponsorbox.com / admin123");
            }
        };
    }
}
