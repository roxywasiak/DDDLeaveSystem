package com.example.leave.application.command;

import com.example.leave.application.dto.AuthResponse;
import com.example.leave.application.dto.LoginCommand;
import com.example.leave.application.dto.RegisterCommand;
import com.example.leave.application.exception.DuplicateEmailException;
import com.example.leave.domain.model.Employee;
import com.example.leave.domain.model.Role;
import com.example.leave.domain.repository.EmployeeRepository;
import com.example.leave.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthCommandHandler {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

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

    public AuthResponse login(LoginCommand command) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(command.getEmail(), command.getPassword())
        );
        return new AuthResponse(jwtService.generateToken(command.getEmail()));
    }
}
