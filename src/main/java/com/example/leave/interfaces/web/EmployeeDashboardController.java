package com.example.leave.interfaces.web;

import com.example.leave.application.dto.CreateLeaveCommand;
import com.example.leave.application.dto.EmployeeDto;
import com.example.leave.application.dto.LeaveAllowanceDto;
import com.example.leave.application.dto.LeaveDto;
import com.example.leave.application.facade.LeaveContextFacade;
import com.example.leave.infrastructure.security.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/employee")
@RequiredArgsConstructor
public class EmployeeDashboardController {

    private final LeaveContextFacade facade;
    private final JwtService jwtService;

    @GetMapping("/dashboard")
    @ResponseBody
    public String dashboard(HttpServletRequest request, HttpServletResponse response) {
        EmployeeDto employee = currentEmployee(request);
        if (employee == null) return redirect(response, "/login");

        LeaveAllowanceDto allowance = facade.getAllowance(employee.getId());
        List<LeaveDto> leaves = facade.getLeavesByEmployee(employee.getId());

        int pct = allowance.getTotalDays() > 0
                ? (int) ((allowance.getUsedDays() * 100.0) / allowance.getTotalDays())
                : 0;

        StringBuilder rows = new StringBuilder();
        for (LeaveDto l : leaves) {
            rows.append("<tr>")
                .append("<td>").append(l.getType()).append("</td>")
                .append("<td>").append(l.getStartDate()).append("</td>")
                .append("<td>").append(l.getEndDate()).append("</td>")
                .append("<td>").append(l.getDurationInDays()).append("</td>")
                .append("<td><span class=\"badge badge-").append(l.getStatus().name().toLowerCase())
                .append("\">").append(l.getStatus()).append("</span></td>")
                .append("<td>").append(l.getReason() != null ? escHtml(l.getReason()) : "").append("</td>")
                .append("</tr>");
        }

        return "<!DOCTYPE html><html lang=\"en\"><head>"
            + "<meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
            + "<title>Employee Dashboard &mdash; Leave System</title>"
            + "<link rel=\"stylesheet\" href=\"/css/style.css\"></head><body>"
            + nav(employee.getName(), "EMPLOYEE")
            + "<main class=\"container\" id=\"main-content\">"
            + "<div class=\"card\">"
            + "<h2>Leave Allowance " + allowance.getYear() + "</h2>"
            + "<div class=\"allowance-stats\">"
            + stat("Total", allowance.getTotalDays())
            + stat("Used", allowance.getUsedDays())
            + stat("Remaining", allowance.getRemainingDays())
            + "</div>"
            + "<div class=\"allowance-bar\" role=\"progressbar\" aria-valuenow=\"" + allowance.getUsedDays()
            + "\" aria-valuemin=\"0\" aria-valuemax=\"" + allowance.getTotalDays()
            + "\" aria-label=\"Leave used: " + allowance.getUsedDays() + " of " + allowance.getTotalDays() + " days\">"
            + "<div class=\"allowance-bar-fill\" style=\"width:" + pct + "%\"></div></div>"
            + "</div>"
            + "<div class=\"card\">"
            + "<h2>Request Leave</h2>"
            + "<form action=\"/employee/leave\" method=\"post\">"
            + "<div class=\"form-row\">"
            + "<div class=\"form-group\"><label for=\"type\">Type</label>"
            + "<select id=\"type\" name=\"type\" required>"
            + "<option value=\"ANNUAL\">Annual</option>"
            + "<option value=\"SICK\">Sick</option>"
            + "<option value=\"UNPAID\">Unpaid</option>"
            + "</select></div>"
            + "<div class=\"form-group\"><label for=\"startDate\">Start Date</label>"
            + "<input type=\"date\" id=\"startDate\" name=\"startDate\" required></div>"
            + "<div class=\"form-group\"><label for=\"endDate\">End Date</label>"
            + "<input type=\"date\" id=\"endDate\" name=\"endDate\" required></div>"
            + "</div>"
            + "<div class=\"form-group\"><label for=\"reason\">Reason (optional)</label>"
            + "<input type=\"text\" id=\"reason\" name=\"reason\" maxlength=\"500\"></div>"
            + "<button type=\"submit\" class=\"btn btn-primary\">Submit Request</button>"
            + "</form></div>"
            + "<div class=\"card\"><h2>My Leave History</h2>"
            + "<table><thead><tr>"
            + "<th scope=\"col\">Type</th><th scope=\"col\">Start</th><th scope=\"col\">End</th>"
            + "<th scope=\"col\">Days</th><th scope=\"col\">Status</th><th scope=\"col\">Reason</th>"
            + "</tr></thead><tbody>" + rows + "</tbody></table></div>"
            + "</main></body></html>";
    }

    @PostMapping("/leave")
    public String submitLeave(@RequestParam String type,
                               @RequestParam String startDate,
                               @RequestParam String endDate,
                               @RequestParam(required = false) String reason,
                               HttpServletRequest request,
                               HttpServletResponse response) {
        EmployeeDto employee = currentEmployee(request);
        if (employee == null) return "redirect:/login";
        try {
            CreateLeaveCommand cmd = new CreateLeaveCommand();
            cmd.setType(com.example.leave.domain.model.LeaveType.valueOf(type));
            cmd.setStartDate(LocalDate.parse(startDate));
            cmd.setEndDate(LocalDate.parse(endDate));
            cmd.setReason(reason);
            facade.createLeave(employee.getId(), cmd);
            return "redirect:/employee/dashboard?message=Leave+request+submitted.";
        } catch (Exception e) {
            return "redirect:/employee/dashboard?error=" + encode(e.getMessage());
        }
    }

    private EmployeeDto currentEmployee(HttpServletRequest request) {
        try {
            String token = extractToken(request);
            if (token == null) return null;
            String email = jwtService.extractEmail(token);
            return facade.getEmployeeByEmail(email);
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

    private String stat(String label, int value) {
        return "<div class=\"allowance-stat\"><span>" + label + "</span><strong>" + value + "</strong></div>";
    }

    private String escHtml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private String encode(String s) {
        if (s == null) return "";
        return s.replaceAll("[^a-zA-Z0-9 .,]", "").replace(" ", "+");
    }

    private String nav(String name, String role) {
        return "<nav><a class=\"brand\" href=\"/\" aria-label=\"Leave System home\">Leave System</a>"
            + "<div class=\"nav-links\">"
            + "<span style=\"color:#ccc;font-size:0.9rem\">Hello, " + escHtml(name) + "</span>"
            + "<a href=\"/logout\">Sign Out</a>"
            + "</div></nav>";
    }
}
