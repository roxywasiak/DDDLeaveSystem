package com.example.leave.interfaces.web;

import com.example.leave.application.dto.EmployeeDto;
import com.example.leave.application.dto.LeaveDto;
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
@RequestMapping("/manager")
@RequiredArgsConstructor
public class ManagerDashboardController {

    private final LeaveContextFacade facade;
    private final JwtService jwtService;

    @GetMapping("/dashboard")
    @ResponseBody
    public String dashboard(HttpServletRequest request, HttpServletResponse response) {
        EmployeeDto employee = currentEmployee(request);
        if (employee == null) return redirect(response, "/login");
        if (!"MANAGER".equals(employee.getRole().name())) return redirect(response, "/employee/dashboard");

        List<LeaveDto> pending = facade.getPendingLeaves();

        StringBuilder rows = new StringBuilder();
        for (LeaveDto l : pending) {
            EmployeeDto emp = safeGetEmployee(l.getEmployeeId());
            String empName = emp != null ? escHtml(emp.getName()) : "Employee #" + l.getEmployeeId();
            rows.append("<tr>")
                .append("<td>").append(empName).append("</td>")
                .append("<td>").append(l.getType()).append("</td>")
                .append("<td>").append(l.getStartDate()).append("</td>")
                .append("<td>").append(l.getEndDate()).append("</td>")
                .append("<td>").append(l.getDurationInDays()).append("</td>")
                .append("<td>").append(l.getReason() != null ? escHtml(l.getReason()) : "").append("</td>")
                .append("<td class=\"action-cell\">")
                .append("<form action=\"/manager/leave/").append(l.getId()).append("/approve\" method=\"post\" style=\"display:inline\">")
                .append("<button type=\"submit\" class=\"btn btn-success btn-sm\">Approve</button></form> ")
                .append("<form action=\"/manager/leave/").append(l.getId()).append("/reject\" method=\"post\" style=\"display:inline\">")
                .append("<button type=\"submit\" class=\"btn btn-danger btn-sm\">Reject</button></form>")
                .append("</td></tr>");
        }

        String emptyMsg = pending.isEmpty()
            ? "<tr><td colspan=\"7\" style=\"text-align:center;color:#595959\">No pending requests</td></tr>"
            : rows.toString();

        return "<!DOCTYPE html><html lang=\"en\"><head>"
            + "<meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
            + "<title>Manager Dashboard &mdash; Leave System</title>"
            + "<link rel=\"stylesheet\" href=\"/css/style.css\"></head><body>"
            + nav(employee.getName())
            + "<main class=\"container\" id=\"main-content\">"
            + "<div class=\"card\"><h2>Pending Leave Requests</h2>"
            + "<table><thead><tr>"
            + "<th scope=\"col\">Employee</th><th scope=\"col\">Type</th>"
            + "<th scope=\"col\">Start</th><th scope=\"col\">End</th>"
            + "<th scope=\"col\">Days</th><th scope=\"col\">Reason</th>"
            + "<th scope=\"col\">Actions</th>"
            + "</tr></thead><tbody>" + emptyMsg + "</tbody></table>"
            + "</div></main></body></html>";
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
            String token = extractToken(request);
            if (token == null) return null;
            return facade.getEmployeeByEmail(jwtService.extractEmail(token));
        } catch (Exception e) {
            return null;
        }
    }

    private EmployeeDto safeGetEmployee(Long id) {
        try { return facade.getEmployeeById(id); } catch (Exception e) { return null; }
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
