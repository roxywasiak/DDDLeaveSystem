package com.example.leave.interfaces.rest;

import com.example.leave.application.dto.EmployeeDto;
import com.example.leave.application.facade.LeaveContextFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final LeaveContextFacade facade;

    @GetMapping("/me")
    public ResponseEntity<EmployeeDto> getMe(Authentication auth) {
        return ResponseEntity.ok(facade.getEmployeeByEmail(auth.getName()));
    }

    @GetMapping
    public ResponseEntity<Page<EmployeeDto>> getAllEmployees(
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return ResponseEntity.ok(facade.getAllEmployees(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDto> getEmployeeById(@PathVariable Long id) {
        return ResponseEntity.ok(facade.getEmployeeById(id));
    }
}
