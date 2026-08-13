package com.example.leave.domain.event;

public record LeaveRejectedEvent(Long leaveId, Long employeeId) {}
