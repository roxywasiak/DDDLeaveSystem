package com.example.leave.interfaces.web;

import com.example.leave.application.dto.LoginCommand;
import com.example.leave.application.facade.LeaveContextFacade;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
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
            Cookie cookie = new Cookie("jwt", authResponse.getToken());
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(86400);
            cookie.setAttribute("SameSite", "Strict");
            response.addCookie(cookie);

            String redirect = switch (facade.getEmployeeByEmail(email).getRole().name()) {
                case "MANAGER" -> "/manager/dashboard";
                case "ADMIN"   -> "/admin/dashboard";
                default        -> "/employee/dashboard";
            };
            return "redirect:" + redirect;
        } catch (BadCredentialsException e) {
            return "redirect:/login?error=Invalid+email+or+password.";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", "Strict");
        response.addCookie(cookie);
        return "redirect:/login?message=You+have+been+logged+out.";
    }
}
