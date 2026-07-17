package com.example.leave.application.command;

import com.example.leave.application.dto.CreateLeaveCommand;
import com.example.leave.application.dto.LeaveDto;
import com.example.leave.application.exception.LeaveRequestNotFoundException;
import com.example.leave.application.mapper.LeaveMapper;
import com.example.leave.domain.model.Leave;
import com.example.leave.domain.repository.LeaveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class LeaveCommandHandler {

    private final LeaveRepository leaveRepository;
    private final LeaveMapper leaveMapper;

    public LeaveDto createLeave(Long employeeId, CreateLeaveCommand command) {
        Leave leave = new Leave(
                employeeId,
                command.getType(),
                command.getStartDate(),
                command.getEndDate(),
                command.getReason()
        );
        return leaveMapper.toDto(leaveRepository.save(leave));
    }

    public LeaveDto approveLeave(Long leaveId) {
        Leave leave = findLeaveOrThrow(leaveId);
        leave.approve();
        return leaveMapper.toDto(leaveRepository.save(leave));
    }

    public LeaveDto rejectLeave(Long leaveId) {
        Leave leave = findLeaveOrThrow(leaveId);
        leave.reject();
        return leaveMapper.toDto(leaveRepository.save(leave));
    }

    public LeaveDto cancelLeave(Long leaveId, Long employeeId) {
        Leave leave = findLeaveOrThrow(leaveId);
        leave.cancel(employeeId);
        return leaveMapper.toDto(leaveRepository.save(leave));
    }

    private Leave findLeaveOrThrow(Long id) {
        return leaveRepository.findById(id)
                .orElseThrow(() -> new LeaveRequestNotFoundException(id));
    }
}
