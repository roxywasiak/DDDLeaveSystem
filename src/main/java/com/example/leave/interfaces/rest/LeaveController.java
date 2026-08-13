package com.example.leave.interfaces.rest;

import com.example.leave.application.dto.CreateLeaveCommand;
import com.example.leave.application.dto.EmployeeDto;
import com.example.leave.application.dto.LeaveAllowanceDto;
import com.example.leave.application.dto.LeaveDto;
import com.example.leave.application.facade.LeaveContextFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<List<LeaveDto>> getPendingLeaves() {
        return ResponseEntity.ok(facade.getPendingLeaves());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeaveDto> getLeaveById(@PathVariable Long id) {
        return ResponseEntity.ok(facade.getLeaveById(id));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<LeaveDto> approveLeave(@PathVariable Long id) {
        return ResponseEntity.ok(facade.approveLeave(id));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<LeaveDto> rejectLeave(@PathVariable Long id) {
        return ResponseEntity.ok(facade.rejectLeave(id));
    }

    @GetMapping("/allowance")
    public ResponseEntity<LeaveAllowanceDto> getMyAllowance(Authentication auth) {
        EmployeeDto employee = facade.getEmployeeByEmail(auth.getName());
        return ResponseEntity.ok(facade.getAllowance(employee.getId()));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<LeaveDto> cancelLeave(@PathVariable Long id, Authentication auth) {
        EmployeeDto employee = facade.getEmployeeByEmail(auth.getName());
        return ResponseEntity.ok(facade.cancelLeave(id, employee.getId()));
    }
}
