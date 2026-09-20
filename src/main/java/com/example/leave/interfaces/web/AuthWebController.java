package com.example.leave.interfaces.web;

import com.example.leave.application.dto.LoginCommand;
import com.example.leave.application.facade.LeaveContextFacade;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthWebController {

    private final LeaveContextFacade facade;

    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "forward:/login.html";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpServletRequest request,
                        HttpServletResponse response) {
        try {
            var authResponse = facade.login(new LoginCommand(email, password, request.getRemoteAddr()));
            String safeToken = authResponse.getToken().replaceAll("[^A-Za-z0-9._\\-]", "");
            ResponseCookie cookie = ResponseCookie.from("jwt", safeToken)
                    .httpOnly(true)
                    .path("/")
                    .maxAge(86400)
                    .sameSite("Strict")
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            String role = facade.getEmployeeByEmail(email).getRole().name();
            return switch (role) {
                case "MANAGER" -> "redirect:/manager/dashboard";
                case "ADMIN"   -> "redirect:/admin/dashboard";
                default        -> "redirect:/employee/dashboard";
            };
        } catch (BadCredentialsException e) {
            log.warn("Failed login attempt for email: {}", email.replaceAll("[\r\n]", ""), e);
            return "redirect:/login?error=Invalid+email+or+password.";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return "redirect:/login?message=You+have+been+logged+out.";
    }
}
