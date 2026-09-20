package com.example.leave.domain.event;

public record LeaveCancelledEvent(Long leaveId, Long employeeId) {}
