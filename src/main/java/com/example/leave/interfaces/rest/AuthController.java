package com.example.leave.interfaces.rest;

import com.example.leave.application.dto.AuthResponse;
import com.example.leave.application.dto.LoginCommand;
import com.example.leave.application.dto.RegisterCommand;
import com.example.leave.application.facade.LeaveContextFacade;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LeaveContextFacade facade;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterCommand command) {
        return ResponseEntity.status(HttpStatus.CREATED).body(facade.register(command));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginCommand command,
                                               HttpServletRequest request) {
        command.setIpAddress(request.getRemoteAddr());
        return ResponseEntity.ok(facade.login(command));
    }
}
