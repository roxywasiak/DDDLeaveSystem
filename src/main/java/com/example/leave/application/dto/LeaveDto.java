package com.example.leave.application.dto;

import com.example.leave.domain.model.LeaveStatus;
import com.example.leave.domain.model.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class LeaveDto {
    private Long id;
    private Long employeeId;
    private LeaveType type;
    private LocalDate startDate;
    private LocalDate endDate;
    private String reason;
    private LeaveStatus status;
}
