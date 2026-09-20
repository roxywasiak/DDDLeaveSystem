package com.example.leave.interfaces.rest;

import com.example.leave.application.dto.*;
import com.example.leave.application.facade.LeaveContextFacade;
import com.example.leave.domain.model.LeaveStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final LeaveContextFacade facade;

    @GetMapping("/allowances/{employeeId}")
    public ResponseEntity<LeaveAllowanceDto> getAllowance(@PathVariable Long employeeId) {
        return ResponseEntity.ok(facade.getAllowance(employeeId));
    }

    @PutMapping("/allowances/{employeeId}")
    public ResponseEntity<LeaveAllowanceDto> amendAllowance(@PathVariable Long employeeId,
                                                             @Valid @RequestBody AmendAllowanceCommand command) {
        return ResponseEntity.ok(facade.amendAllowance(employeeId, command));
    }

    @PostMapping("/employees")
    public ResponseEntity<EmployeeDto> createEmployee(@Valid @RequestBody AdminCreateEmployeeCommand command) {
        return ResponseEntity.status(HttpStatus.CREATED).body(facade.adminCreateEmployee(command));
    }

    @PutMapping("/employees/{employeeId}")
    public ResponseEntity<EmployeeDto> amendEmployee(@PathVariable Long employeeId,
                                                      @Valid @RequestBody AmendEmployeeCommand command) {
        return ResponseEntity.ok(facade.amendEmployee(employeeId, command));
    }

    @GetMapping("/leaves")
    public ResponseEntity<Page<LeaveDto>> getLeaves(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) LeaveStatus status,
            @PageableDefault(size = 20, sort = "startDate") Pageable pageable) {
        return ResponseEntity.ok(facade.getLeavesByFilter(employeeId, status, pageable));
    }
}
