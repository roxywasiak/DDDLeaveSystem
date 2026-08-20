package com.example.leave.application.query;

import com.example.leave.application.dto.EmployeeLeaveStatsDto;
import com.example.leave.application.dto.LeaveAllowanceDto;
import com.example.leave.application.dto.LeaveDto;
import com.example.leave.application.exception.LeaveAllowanceNotFoundException;
import com.example.leave.application.exception.LeaveRequestNotFoundException;
import com.example.leave.application.mapper.LeaveAllowanceMapper;
import com.example.leave.application.mapper.LeaveMapper;
import com.example.leave.domain.model.Employee;
import com.example.leave.domain.model.Leave;
import com.example.leave.domain.model.LeaveStatus;
import com.example.leave.domain.repository.EmployeeRepository;
import com.example.leave.domain.repository.LeaveAllowanceRepository;
import com.example.leave.domain.repository.LeaveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaveQueryHandler {

    private final LeaveRepository leaveRepository;
    private final LeaveAllowanceRepository leaveAllowanceRepository;
    private final EmployeeRepository employeeRepository;
    private final LeaveMapper leaveMapper;
    private final LeaveAllowanceMapper leaveAllowanceMapper;

    public LeaveDto getLeaveById(Long id) {
        return leaveRepository.findById(id)
                .map(leaveMapper::toDto)
                .orElseThrow(() -> new LeaveRequestNotFoundException(id));
    }

    public List<LeaveDto> getLeavesByEmployee(Long employeeId) {
        return leaveMapper.toDtoList(leaveRepository.findByEmployeeId(employeeId));
    }

    public List<LeaveDto> getPendingLeaves() {
        return leaveMapper.toDtoList(leaveRepository.findByStatus(LeaveStatus.PENDING));
    }

    public List<LeaveDto> getPendingLeavesForManager(Long managerId) {
        List<Long> teamIds = employeeRepository.findByManagerId(managerId)
                .stream().map(e -> e.getId()).toList();
        if (teamIds.isEmpty()) return List.of();
        return leaveMapper.toDtoList(
                leaveRepository.findByStatusAndEmployeeIdIn(LeaveStatus.PENDING, teamIds));
    }

    public List<EmployeeLeaveStatsDto> getTeamStatsForManager(Long managerId) {
        List<Employee> team = employeeRepository.findByManagerId(managerId);
        if (team.isEmpty()) return List.of();
        List<Long> teamIds = team.stream().map(Employee::getId).toList();
        Map<Long, String> nameById = team.stream()
                .collect(Collectors.toMap(Employee::getId, Employee::getName));
        int year = LocalDate.now().getYear();
        Map<Long, Integer> approvedDays = leaveRepository
                .findApprovedByEmployeeIdInAndYear(teamIds, year)
                .stream()
                .collect(Collectors.groupingBy(
                        Leave::getEmployeeId,
                        Collectors.summingInt(Leave::getDurationInDays)
                ));
        return teamIds.stream()
                .map(id -> new EmployeeLeaveStatsDto(
                        id,
                        nameById.get(id),
                        approvedDays.getOrDefault(id, 0)
                ))
                .toList();
    }

    public List<LeaveAllowanceDto> getAllowanceHistory(Long employeeId) {
        return leaveAllowanceMapper.toDtoList(
                leaveAllowanceRepository.findByEmployeeIdOrderByYearDesc(employeeId));
    }

    public LeaveAllowanceDto getAllowance(Long employeeId) {
        int year = LocalDate.now().getYear();
        return leaveAllowanceRepository.findByEmployeeIdAndYear(employeeId, year)
                .map(leaveAllowanceMapper::toDto)
                .orElseThrow(() -> new LeaveAllowanceNotFoundException(employeeId, year));
    }
}
