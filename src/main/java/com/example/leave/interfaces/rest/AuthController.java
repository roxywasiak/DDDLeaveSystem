package com.example.leave.interfaces.rest;

import com.example.leave.application.dto.AuthResponse;
import com.example.leave.application.dto.LoginCommand;
import com.example.leave.application.dto.RegisterCommand;
import com.example.leave.application.facade.LeaveContextFacade;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final LeaveContextFacade facade;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterCommand command) {
        return ResponseEntity.status(HttpStatus.CREATED).body(facade.register(command));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginCommand command,
                                               HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        try {
            AuthResponse response = facade.login(command);
            log.info("AUTH SUCCESS — email={}, ip={}", sanitize(command.getEmail()), sanitize(ip));
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            log.warn("AUTH FAILURE — email={}, ip={}", sanitize(command.getEmail()), sanitize(ip));
            throw e;
        }
    }

    private static String sanitize(String input) {
        if (input == null) return "null";
        return input.replaceAll("[\r\n\t]", "_");
    }
}
