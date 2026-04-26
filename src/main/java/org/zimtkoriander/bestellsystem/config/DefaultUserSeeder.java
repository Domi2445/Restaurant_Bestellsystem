package org.zimtkoriander.bestellsystem.config;

import Model.AppUser;
import Model.Role;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.zimtkoriander.bestellsystem.repository.AppUserRepository;

import java.util.Set;

@Configuration
public class DefaultUserSeeder {

	@Bean
	CommandLineRunner seedDefaultUsers(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			if (!appUserRepository.existsByUsername("admin")) {
				AppUser admin = new AppUser();
				admin.setUsername("admin");
				admin.setEmail("admin@bestellsystem.local");
				admin.setPasswordHash(passwordEncoder.encode("admin123"));
				admin.setActive(true);
				admin.setRoles(Set.of(Role.ADMIN, Role.STAFF));
				appUserRepository.save(admin);
			}
		};
	}
}

