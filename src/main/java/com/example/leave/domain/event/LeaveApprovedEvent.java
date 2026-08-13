package com.example.leave.domain.event;

public record LeaveApprovedEvent(Long leaveId, Long employeeId, int durationInDays) {}
