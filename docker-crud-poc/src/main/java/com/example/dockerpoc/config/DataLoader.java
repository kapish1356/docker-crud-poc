package com.example.dockerpoc.config;

import com.example.dockerpoc.model.Employee;
import com.example.dockerpoc.repository.EmployeeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner seedData(EmployeeRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                repository.save(new Employee("Rahul Sharma", "rahul@example.com", "Engineering", 60000.0));
                repository.save(new Employee("Priya Singh", "priya@example.com", "HR", 45000.0));
            }
        };
    }
}
