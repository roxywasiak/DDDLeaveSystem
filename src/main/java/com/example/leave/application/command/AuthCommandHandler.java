package com.example.leave.application.command;

import com.example.leave.application.dto.AdminCreateEmployeeCommand;
import com.example.leave.application.dto.AmendEmployeeCommand;
import com.example.leave.application.dto.AuthResponse;
import com.example.leave.application.dto.LoginCommand;
import com.example.leave.application.dto.RegisterCommand;
import com.example.leave.application.exception.DuplicateEmailException;
import com.example.leave.application.exception.EmployeeNotFoundException;
import com.example.leave.application.mapper.EmployeeMapper;
import com.example.leave.domain.model.Employee;
import com.example.leave.application.dto.EmployeeDto;
import com.example.leave.domain.model.Role;
import com.example.leave.domain.repository.EmployeeRepository;
import com.example.leave.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AuthCommandHandler {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmployeeMapper employeeMapper;

    public AuthResponse register(RegisterCommand command) {
        if (employeeRepository.existsByEmail(command.getEmail())) {
            throw new DuplicateEmailException(command.getEmail());
        }
        Employee employee = new Employee(
                command.getEmail(),
                passwordEncoder.encode(command.getPassword()),
                command.getName(),
                Role.EMPLOYEE
        );
        employeeRepository.save(employee);
        return new AuthResponse(jwtService.generateToken(employee.getEmail()));
    }

    public EmployeeDto adminCreateEmployee(AdminCreateEmployeeCommand command) {
        if (employeeRepository.existsByEmail(command.getEmail())) {
            throw new DuplicateEmailException(command.getEmail());
        }
        Employee employee = new Employee(
                command.getEmail(),
                passwordEncoder.encode(command.getPassword()),
                command.getName(),
                command.getRole(),
                command.getDepartment()
        );
        if (command.getManagerId() != null) {
            employee.assignManager(command.getManagerId());
        }
        return employeeMapper.toDto(employeeRepository.save(employee));
    }

    public EmployeeDto amendEmployee(Long employeeId, AmendEmployeeCommand command) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));
        employee.updateRoleAndDepartment(command.getRole(), command.getDepartment());
        return employeeMapper.toDto(employeeRepository.save(employee));
    }

    public AuthResponse login(LoginCommand command) {
        String email = sanitize(command.getEmail());
        String ip = sanitize(command.getIpAddress());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(command.getEmail(), command.getPassword())
            );
            log.info("AUTH SUCCESS — timestamp={}, email={}, ip={}", Instant.now(), email, ip);
            return new AuthResponse(jwtService.generateToken(command.getEmail()));
        } catch (BadCredentialsException e) {
            log.warn("AUTH FAILURE — timestamp={}, email={}, ip={}", Instant.now(), email, ip);
            throw e;
        }
    }

    private static String sanitize(String input) {
        if (input == null) return "unknown";
        return input.replaceAll("[\r\n\t]", "_");
    }
}
