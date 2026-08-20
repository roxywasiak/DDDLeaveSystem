package com.example.leave.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmployeeLeaveStatsDto {
    private Long employeeId;
    private String employeeName;
    private int approvedDaysThisYear;
}
