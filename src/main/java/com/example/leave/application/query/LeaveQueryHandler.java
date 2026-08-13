package com.example.leave.application.query;

import com.example.leave.application.dto.LeaveAllowanceDto;
import com.example.leave.application.dto.LeaveDto;
import com.example.leave.application.exception.LeaveAllowanceNotFoundException;
import com.example.leave.application.exception.LeaveRequestNotFoundException;
import com.example.leave.application.mapper.LeaveAllowanceMapper;
import com.example.leave.application.mapper.LeaveMapper;
import com.example.leave.domain.model.LeaveStatus;
import com.example.leave.domain.repository.LeaveAllowanceRepository;
import com.example.leave.domain.repository.LeaveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaveQueryHandler {

    private final LeaveRepository leaveRepository;
    private final LeaveAllowanceRepository leaveAllowanceRepository;
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

    public LeaveAllowanceDto getAllowance(Long employeeId) {
        int year = LocalDate.now().getYear();
        return leaveAllowanceRepository.findByEmployeeIdAndYear(employeeId, year)
                .map(leaveAllowanceMapper::toDto)
                .orElseThrow(() -> new LeaveAllowanceNotFoundException(employeeId, year));
    }
}
