package com.example.leave.application.query;

import com.example.leave.application.dto.LeaveDto;
import com.example.leave.application.exception.LeaveRequestNotFoundException;
import com.example.leave.application.mapper.LeaveMapper;
import com.example.leave.domain.model.LeaveStatus;
import com.example.leave.domain.repository.LeaveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeaveQueryHandler {

    private final LeaveRepository leaveRepository;
    private final LeaveMapper leaveMapper;

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
}
