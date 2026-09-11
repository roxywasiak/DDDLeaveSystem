package com.example.leave.application.facade;

import com.example.leave.application.command.AuthCommandHandler;
import com.example.leave.application.command.LeaveCommandHandler;
import com.example.leave.application.dto.*;
import com.example.leave.application.query.EmployeeQueryHandler;
import com.example.leave.application.query.LeaveQueryHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public LeaveDto approveLeave(Long leaveId, String decidedBy) {
        return leaveCommandHandler.approveLeave(leaveId, decidedBy);
    }

    public LeaveDto rejectLeave(Long leaveId, String reason, String decidedBy) {
        return leaveCommandHandler.rejectLeave(leaveId, reason, decidedBy);
    }

    public LeaveDto amendLeave(Long leaveId, Long employeeId, AmendLeaveCommand command) {
        return leaveCommandHandler.amendLeave(leaveId, employeeId, command);
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

    public Page<LeaveDto> getPendingLeavesForManager(Long managerId, Pageable pageable) {
        return leaveQueryHandler.getPendingLeavesForManager(managerId, pageable);
    }

    public List<EmployeeLeaveStatsDto> getTeamStatsForManager(Long managerId) {
        return leaveQueryHandler.getTeamStatsForManager(managerId);
    }

    public List<LeaveAllowanceDto> getAllowanceHistory(Long employeeId) {
        return leaveQueryHandler.getAllowanceHistory(employeeId);
    }

    public LeaveAllowanceDto getAllowance(Long employeeId) {
        return leaveQueryHandler.getAllowance(employeeId);
    }

    public LeaveAllowanceDto amendAllowance(Long employeeId, AmendAllowanceCommand command) {
        return leaveCommandHandler.amendAllowance(employeeId, command);
    }

    // Employee queries
    public Page<EmployeeDto> getAllEmployees(Pageable pageable) {
        return employeeQueryHandler.getAllEmployees(pageable);
    }

    public EmployeeDto getEmployeeById(Long id) {
        return employeeQueryHandler.getEmployeeById(id);
    }

    public EmployeeDto getEmployeeByEmail(String email) {
        return employeeQueryHandler.getEmployeeByEmail(email);
    }
}
