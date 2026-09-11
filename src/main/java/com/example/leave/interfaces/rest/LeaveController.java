package com.example.leave.interfaces.rest;

import com.example.leave.application.dto.AmendLeaveCommand;
import com.example.leave.application.dto.CreateLeaveCommand;
import com.example.leave.application.dto.EmployeeDto;
import com.example.leave.application.dto.EmployeeLeaveStatsDto;
import com.example.leave.application.dto.LeaveAllowanceDto;
import com.example.leave.application.dto.LeaveDto;
import com.example.leave.application.facade.LeaveContextFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveContextFacade facade;

    @PostMapping
    public ResponseEntity<LeaveDto> createLeave(@Valid @RequestBody CreateLeaveCommand command,
                                                 Authentication auth) {
        EmployeeDto employee = facade.getEmployeeByEmail(auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(facade.createLeave(employee.getId(), command));
    }

    @GetMapping("/my")
    public ResponseEntity<List<LeaveDto>> getMyLeaves(Authentication auth) {
        EmployeeDto employee = facade.getEmployeeByEmail(auth.getName());
        return ResponseEntity.ok(facade.getLeavesByEmployee(employee.getId()));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<Page<LeaveDto>> getPendingLeaves(
            @PageableDefault(size = 20, sort = "startDate") Pageable pageable,
            Authentication auth) {
        EmployeeDto manager = facade.getEmployeeByEmail(auth.getName());
        return ResponseEntity.ok(facade.getPendingLeavesForManager(manager.getId(), pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveDto> getLeaveById(@PathVariable Long id) {
        return ResponseEntity.ok(facade.getLeaveById(id));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<LeaveDto> approveLeave(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(facade.approveLeave(id, auth.getName()));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<LeaveDto> rejectLeave(@PathVariable Long id,
                                                 @RequestBody(required = false) Map<String, String> body,
                                                 Authentication auth) {
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(facade.rejectLeave(id, reason, auth.getName()));
    }

    @PutMapping("/{id}/amend")
    public ResponseEntity<LeaveDto> amendLeave(@PathVariable Long id,
                                                @Valid @RequestBody AmendLeaveCommand command,
                                                Authentication auth) {
        EmployeeDto employee = facade.getEmployeeByEmail(auth.getName());
        return ResponseEntity.ok(facade.amendLeave(id, employee.getId(), command));
    }

    @GetMapping("/allowance")
    public ResponseEntity<LeaveAllowanceDto> getMyAllowance(Authentication auth) {
        EmployeeDto employee = facade.getEmployeeByEmail(auth.getName());
        return ResponseEntity.ok(facade.getAllowance(employee.getId()));
    }

    @GetMapping("/allowance/history")
    public ResponseEntity<List<LeaveAllowanceDto>> getAllowanceHistory(Authentication auth) {
        EmployeeDto employee = facade.getEmployeeByEmail(auth.getName());
        return ResponseEntity.ok(facade.getAllowanceHistory(employee.getId()));
    }

    @GetMapping("/team-stats")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<EmployeeLeaveStatsDto>> getTeamStats(Authentication auth) {
        EmployeeDto manager = facade.getEmployeeByEmail(auth.getName());
        return ResponseEntity.ok(facade.getTeamStatsForManager(manager.getId()));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<LeaveDto> cancelLeave(@PathVariable Long id, Authentication auth) {
        EmployeeDto employee = facade.getEmployeeByEmail(auth.getName());
        return ResponseEntity.ok(facade.cancelLeave(id, employee.getId()));
    }
}
