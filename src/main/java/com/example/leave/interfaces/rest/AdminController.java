package com.example.leave.interfaces.rest;

import com.example.leave.application.dto.AmendAllowanceCommand;
import com.example.leave.application.dto.LeaveAllowanceDto;
import com.example.leave.application.facade.LeaveContextFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final LeaveContextFacade facade;

    @PutMapping("/allowances/{employeeId}")
    public ResponseEntity<LeaveAllowanceDto> amendAllowance(@PathVariable Long employeeId,
                                                             @Valid @RequestBody AmendAllowanceCommand command) {
        return ResponseEntity.ok(facade.amendAllowance(employeeId, command));
    }
}
