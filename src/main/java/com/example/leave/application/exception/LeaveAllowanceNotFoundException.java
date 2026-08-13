package com.example.leave.application.exception;

public class LeaveAllowanceNotFoundException extends RuntimeException {
    public LeaveAllowanceNotFoundException(Long employeeId, int year) {
        super("Leave allowance not found for employee " + employeeId + " in year " + year);
    }
}
