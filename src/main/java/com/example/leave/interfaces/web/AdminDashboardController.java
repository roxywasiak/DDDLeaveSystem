package com.example.leave.interfaces.web;

import com.example.leave.application.dto.AmendAllowanceCommand;
import com.example.leave.application.dto.EmployeeDto;
import com.example.leave.application.exception.EmployeeNotFoundException;
import com.example.leave.application.exception.LeaveAllowanceNotFoundException;
import com.example.leave.application.facade.LeaveContextFacade;
import com.example.leave.infrastructure.security.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardController {

    private final LeaveContextFacade facade;
    private final JwtService jwtService;

    @GetMapping("/dashboard")
    public String dashboard(HttpServletRequest request) {
        EmployeeDto employee = currentEmployee(request);
        if (employee == null) return "redirect:/login";
        if (!"ADMIN".equals(employee.getRole().name())) return "redirect:/employee/dashboard";
        return "forward:/admin/dashboard.html";
    }

    @PostMapping("/allowance/{employeeId}")
    public String amendAllowance(@PathVariable Long employeeId,
                                  @RequestParam int totalDays,
                                  HttpServletRequest request) {
        if (currentEmployee(request) == null) return "redirect:/login";
        try {
            facade.amendAllowance(employeeId, new AmendAllowanceCommand(totalDays));
            return "redirect:/admin/dashboard?message=Allowance+updated.";
        } catch (LeaveAllowanceNotFoundException | IllegalArgumentException e) {
            return "redirect:/admin/dashboard?error=" + encode(e.getMessage());
        }
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
        } catch (JwtException | EmployeeNotFoundException e) {
            log.warn("Could not resolve current employee from JWT", e);
            return null;
        }
    }

    private String encode(String s) {
        if (s == null) return "";
        return s.replaceAll("[^a-zA-Z0-9 .,]", "").replace(" ", "+");
    }
}
