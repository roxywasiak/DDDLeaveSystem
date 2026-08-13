package com.example.leave.application.dto;

import lombok.Data;

@Data
public class LeaveAllowanceDto {
    private Long id;
    private Long employeeId;
    private int year;
    private int totalDays;
    private int usedDays;
    private int remainingDays;
}
