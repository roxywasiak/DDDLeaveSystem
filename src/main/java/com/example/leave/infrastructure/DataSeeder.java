package com.example.leave.infrastructure;

import com.example.leave.domain.model.Employee;
import com.example.leave.domain.model.Role;
import com.example.leave.domain.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (employeeRepository.count() == 0) {
            employeeRepository.save(new Employee(
                    "manager@example.com",
                    passwordEncoder.encode("password123"),
                    "Jane Manager",
                    Role.MANAGER
            ));
            employeeRepository.save(new Employee(
                    "employee@example.com",
                    passwordEncoder.encode("password123"),
                    "John Doe",
                    Role.EMPLOYEE
            ));
            System.out.println("=== Seeded default users ===");
            System.out.println("Manager:  manager@example.com / password123");
            System.out.println("Employee: employee@example.com / password123");
        }
    }
}
