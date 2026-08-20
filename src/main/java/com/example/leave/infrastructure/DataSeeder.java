package com.example.leave.infrastructure;

import com.example.leave.domain.model.Employee;
import com.example.leave.domain.model.LeaveAllowance;
import com.example.leave.domain.model.Role;
import com.example.leave.domain.repository.EmployeeRepository;
import com.example.leave.domain.repository.LeaveAllowanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder {

    private final EmployeeRepository employeeRepository;
    private final LeaveAllowanceRepository leaveAllowanceRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seed() {
        if (employeeRepository.count() == 0) {
            int year = LocalDate.now().getYear();

            Employee manager = employeeRepository.save(new Employee(
                    "manager@example.com",
                    passwordEncoder.encode("password123"),
                    "Jane Manager",
                    Role.MANAGER
            ));
            Employee employee = employeeRepository.save(new Employee(
                    "employee@example.com",
                    passwordEncoder.encode("password123"),
                    "John Doe",
                    Role.EMPLOYEE
            ));
            employee.assignManager(manager.getId());
            employeeRepository.save(employee);
            leaveAllowanceRepository.save(new LeaveAllowance(manager.getId(), year, 25));
            leaveAllowanceRepository.save(new LeaveAllowance(employee.getId(), year, 25));

            employeeRepository.save(new Employee(
                    "admin@example.com",
                    passwordEncoder.encode("password123"),
                    "System Admin",
                    Role.ADMIN
            ));

            log.info("=== Seeded default users ===");
            log.info("Manager:  manager@example.com / password123");
            log.info("Employee: employee@example.com / password123");
            log.info("Admin:    admin@example.com / password123");
        }
    }
}
