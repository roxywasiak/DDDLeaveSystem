package com.example.leave.application.dto;

import com.example.leave.domain.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmployeeDto {
    private Long id;
    private String email;
    private String name;
    private Role role;
    private Long managerId;
    private String department;
}
