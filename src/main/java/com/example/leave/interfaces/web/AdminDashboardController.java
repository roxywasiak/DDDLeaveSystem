package com.example.leave.interfaces.web;

import com.example.leave.application.dto.AmendAllowanceCommand;
import com.example.leave.application.dto.EmployeeDto;
import com.example.leave.application.facade.LeaveContextFacade;
import com.example.leave.infrastructure.security.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final LeaveContextFacade facade;
    private final JwtService jwtService;

    @GetMapping("/dashboard")
    @ResponseBody
    public String dashboard(HttpServletRequest request, HttpServletResponse response) {
        EmployeeDto employee = currentEmployee(request);
        if (employee == null) return redirect(response, "/login");
        if (!"ADMIN".equals(employee.getRole().name())) return redirect(response, "/employee/dashboard");

        List<EmployeeDto> employees = facade.getAllEmployees();

        StringBuilder rows = new StringBuilder();
        for (EmployeeDto emp : employees) {
            String allowanceInfo = getAllowanceInfo(emp.getId());
            rows.append("<tr>")
                .append("<td>").append(escHtml(emp.getName())).append("</td>")
                .append("<td>").append(escHtml(emp.getEmail())).append("</td>")
                .append("<td><span class=\"badge badge-").append(emp.getRole().name().toLowerCase())
                .append("\">").append(emp.getRole()).append("</span></td>")
                .append("<td>").append(allowanceInfo).append("</td>")
                .append("<td>")
                .append("<form action=\"/admin/allowance/").append(emp.getId()).append("\" method=\"post\" style=\"display:flex;gap:0.5rem;align-items:center\">")
                .append("<label for=\"days-").append(emp.getId()).append("\" class=\"sr-only\">New allowance days for ").append(escHtml(emp.getName())).append("</label>")
                .append("<input type=\"number\" id=\"days-").append(emp.getId()).append("\" name=\"totalDays\" min=\"1\" max=\"365\" style=\"width:70px;padding:0.3rem\" required>")
                .append("<button type=\"submit\" class=\"btn btn-primary btn-sm\">Update</button>")
                .append("</form>")
                .append("</td></tr>");
        }

        return "<!DOCTYPE html><html lang=\"en\"><head>"
            + "<meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
            + "<title>Admin Dashboard &mdash; Leave System</title>"
            + "<link rel=\"stylesheet\" href=\"/css/style.css\"></head><body>"
            + nav(employee.getName())
            + "<main class=\"container\" id=\"main-content\">"
            + "<div class=\"card\"><h2>Employee Allowances</h2>"
            + "<table><thead><tr>"
            + "<th scope=\"col\">Name</th><th scope=\"col\">Email</th>"
            + "<th scope=\"col\">Role</th><th scope=\"col\">Allowance</th>"
            + "<th scope=\"col\">Amend</th>"
            + "</tr></thead><tbody>" + rows + "</tbody></table>"
            + "</div></main></body></html>";
    }

    @PostMapping("/allowance/{employeeId}")
    public String amendAllowance(@PathVariable Long employeeId,
                                  @RequestParam int totalDays,
                                  HttpServletRequest request) {
        if (currentEmployee(request) == null) return "redirect:/login";
        try {
            facade.amendAllowance(employeeId, new AmendAllowanceCommand(totalDays));
            return "redirect:/admin/dashboard?message=Allowance+updated.";
        } catch (Exception e) {
            return "redirect:/admin/dashboard?error=" + encode(e.getMessage());
        }
    }

    private String getAllowanceInfo(Long employeeId) {
        try {
            var a = facade.getAllowance(employeeId);
            return a.getRemainingDays() + " / " + a.getTotalDays() + " days remaining";
        } catch (Exception e) {
            return "<span style=\"color:#595959\">No allowance set</span>";
        }
    }

    private EmployeeDto currentEmployee(HttpServletRequest request) {
        try {
            String token = extractToken(request);
            if (token == null) return null;
            return facade.getEmployeeByEmail(jwtService.extractEmail(token));
        } catch (Exception e) {
            return null;
        }
    }

    private String extractToken(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> "jwt".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst().orElse(null);
    }

    private String redirect(HttpServletResponse response, String url) {
        response.setHeader("Location", url);
        response.setStatus(302);
        return "";
    }

    private String escHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private String encode(String s) {
        if (s == null) return "";
        return s.replaceAll("[^a-zA-Z0-9 .,]", "").replace(" ", "+");
    }

    private String nav(String name) {
        return "<nav><a class=\"brand\" href=\"/\" aria-label=\"Leave System home\">Leave System</a>"
            + "<div class=\"nav-links\">"
            + "<span style=\"color:#ccc;font-size:0.9rem\">Hello, " + escHtml(name) + "</span>"
            + "<a href=\"/logout\">Sign Out</a>"
            + "</div></nav>";
    }
}
