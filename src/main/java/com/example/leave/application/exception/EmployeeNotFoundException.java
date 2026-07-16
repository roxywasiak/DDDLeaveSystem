package com.example.leave.application.exception;

public class EmployeeNotFoundException extends RuntimeException {
    public EmployeeNotFoundException(Long id) {
        super("Employee not found with id: " + id);
    }

    public EmployeeNotFoundException(String email) {
        super("Employee not found with email: " + email);
    }
}
