package com.example.leave.interfaces.web;

import com.example.leave.application.dto.EmployeeDto;
import com.example.leave.application.facade.LeaveContextFacade;
import com.example.leave.infrastructure.security.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;

@Controller
@RequestMapping("/employee")
@RequiredArgsConstructor
public class EmployeeDashboardController {

    private final LeaveContextFacade facade;
    private final JwtService jwtService;

    @GetMapping("/dashboard")
    public String dashboard(HttpServletRequest request, HttpServletResponse response) {
        if (currentEmployee(request) == null) return "redirect:/login";
        return "forward:/employee/dashboard.html";
    }

    private EmployeeDto currentEmployee(HttpServletRequest request) {
        try {
            if (request.getCookies() == null) return null;
            String token = Arrays.stream(request.getCookies())
                    .filter(c -> "jwt".equals(c.getName()))
                    .map(Cookie::getValue)
                    .findFirst().orElse(null);
            if (token == null) return null;
            return facade.getEmployeeByEmail(jwtService.extractEmail(token));
        } catch (Exception e) {
            return null;
        }
    }
}
