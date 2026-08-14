package com.example.leave.interfaces.web;

import com.example.leave.application.dto.EmployeeDto;
import com.example.leave.application.facade.LeaveContextFacade;
import com.example.leave.infrastructure.security.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@Controller
@RequestMapping("/manager")
@RequiredArgsConstructor
public class ManagerDashboardController {

    private final LeaveContextFacade facade;
    private final JwtService jwtService;

    @GetMapping("/dashboard")
    public String dashboard(HttpServletRequest request) {
        EmployeeDto employee = currentEmployee(request);
        if (employee == null) return "redirect:/login";
        if (!"MANAGER".equals(employee.getRole().name())) return "redirect:/employee/dashboard";
        return "forward:/manager/dashboard.html";
    }

    @PostMapping("/leave/{id}/approve")
    public String approve(@PathVariable Long id, HttpServletRequest request) {
        if (currentEmployee(request) == null) return "redirect:/login";
        try {
            facade.approveLeave(id);
            return "redirect:/manager/dashboard?message=Leave+approved.";
        } catch (Exception e) {
            return "redirect:/manager/dashboard?error=" + encode(e.getMessage());
        }
    }

    @PostMapping("/leave/{id}/reject")
    public String reject(@PathVariable Long id, HttpServletRequest request) {
        if (currentEmployee(request) == null) return "redirect:/login";
        try {
            facade.rejectLeave(id);
            return "redirect:/manager/dashboard?message=Leave+rejected.";
        } catch (Exception e) {
            return "redirect:/manager/dashboard?error=" + encode(e.getMessage());
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
        } catch (Exception e) {
            return null;
        }
    }

    private String encode(String s) {
        if (s == null) return "";
        return s.replaceAll("[^a-zA-Z0-9 .,]", "").replace(" ", "+");
    }
}
