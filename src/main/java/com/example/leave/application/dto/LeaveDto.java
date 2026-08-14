package com.example.leave.application.dto;

import com.example.leave.domain.model.LeaveStatus;
import com.example.leave.domain.model.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private String rejectionReason;
    private LocalDateTime createdAt;
    private LocalDateTime decidedAt;
    private String decidedBy;

    public int getDurationInDays() {
        return (int) (endDate.toEpochDay() - startDate.toEpochDay()) + 1;
    }
}
