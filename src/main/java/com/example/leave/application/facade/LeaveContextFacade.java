package com.example.leave.application.facade;

import com.example.leave.application.command.AuthCommandHandler;
import com.example.leave.application.command.LeaveCommandHandler;
import com.example.leave.application.dto.*;
import com.example.leave.application.query.EmployeeQueryHandler;
import com.example.leave.application.query.LeaveQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveContextFacade {

    private final LeaveCommandHandler leaveCommandHandler;
    private final LeaveQueryHandler leaveQueryHandler;
    private final EmployeeQueryHandler employeeQueryHandler;
    private final AuthCommandHandler authCommandHandler;

    // Auth commands
    public AuthResponse register(RegisterCommand command) {
        return authCommandHandler.register(command);
    }

    public AuthResponse login(LoginCommand command) {
        return authCommandHandler.login(command);
    }

    // Leave commands
    public LeaveDto createLeave(Long employeeId, CreateLeaveCommand command) {
        return leaveCommandHandler.createLeave(employeeId, command);
    }

    public LeaveDto approveLeave(Long leaveId) {
        return leaveCommandHandler.approveLeave(leaveId);
    }

    public LeaveDto rejectLeave(Long leaveId) {
        return leaveCommandHandler.rejectLeave(leaveId);
    }

    public LeaveDto cancelLeave(Long leaveId, Long employeeId) {
        return leaveCommandHandler.cancelLeave(leaveId, employeeId);
    }

    // Leave queries
    public LeaveDto getLeaveById(Long id) {
        return leaveQueryHandler.getLeaveById(id);
    }

    public List<LeaveDto> getLeavesByEmployee(Long employeeId) {
        return leaveQueryHandler.getLeavesByEmployee(employeeId);
    }

    public List<LeaveDto> getPendingLeaves() {
        return leaveQueryHandler.getPendingLeaves();
    }

    public LeaveAllowanceDto getAllowance(Long employeeId) {
        return leaveQueryHandler.getAllowance(employeeId);
    }

    public LeaveAllowanceDto amendAllowance(Long employeeId, AmendAllowanceCommand command) {
        return leaveCommandHandler.amendAllowance(employeeId, command);
    }

    // Employee queries
    public List<EmployeeDto> getAllEmployees() {
        return employeeQueryHandler.getAllEmployees();
    }

    public EmployeeDto getEmployeeById(Long id) {
        return employeeQueryHandler.getEmployeeById(id);
    }

    public EmployeeDto getEmployeeByEmail(String email) {
        return employeeQueryHandler.getEmployeeByEmail(email);
    }
}
