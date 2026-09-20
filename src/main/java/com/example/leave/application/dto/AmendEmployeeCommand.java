package com.example.leave.application.dto;

import com.example.leave.domain.model.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AmendEmployeeCommand {
    @NotNull
    private Role role;
    private String department;
}
