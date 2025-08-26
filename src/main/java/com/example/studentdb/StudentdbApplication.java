package com.example.studentdb;

import com.example.studentdb.entity.User;
import com.example.studentdb.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class StudentdbApplication {

	public static void main(String[] args) {
		SpringApplication.run(StudentdbApplication.class, args);
	}
	@Bean
	CommandLineRunner run(UserRepository userRepository, PasswordEncoder encoder) {
		return args -> {
			if(userRepository.findByUsername("admin") == null) {
				User admin = new User();
				admin.setUsername("admin");
				admin.setPassword(encoder.encode("admin123"));
				admin.setRole("ADMIN");
				userRepository.save(admin);
			}
			if(userRepository.findByUsername("student1") == null) {
				User student = new User();
				student.setUsername("student1");
				student.setPassword(encoder.encode("student123"));
				student.setRole("STUDENT");
				userRepository.save(student);
			}
		};
	}

}



