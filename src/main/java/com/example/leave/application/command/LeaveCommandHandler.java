package com.example.leave.application.command;

import com.example.leave.application.dto.AmendAllowanceCommand;
import com.example.leave.application.dto.AmendLeaveCommand;
import com.example.leave.application.dto.CreateLeaveCommand;
import com.example.leave.application.dto.LeaveAllowanceDto;
import com.example.leave.application.dto.LeaveDto;
import com.example.leave.application.exception.LeaveAllowanceNotFoundException;
import com.example.leave.application.exception.LeaveRequestNotFoundException;
import com.example.leave.application.mapper.LeaveAllowanceMapper;
import com.example.leave.application.mapper.LeaveMapper;
import com.example.leave.domain.event.LeaveApprovedEvent;
import com.example.leave.domain.event.LeaveRejectedEvent;
import com.example.leave.domain.model.Leave;
import com.example.leave.domain.model.RejectionReason;
import com.example.leave.domain.model.LeaveAllowance;
import com.example.leave.domain.repository.LeaveAllowanceRepository;
import com.example.leave.domain.repository.LeaveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class LeaveCommandHandler {

    private final LeaveRepository leaveRepository;
    private final LeaveAllowanceRepository leaveAllowanceRepository;
    private final LeaveMapper leaveMapper;
    private final LeaveAllowanceMapper leaveAllowanceMapper;
    private final ApplicationEventPublisher eventPublisher;

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

    public LeaveDto approveLeave(Long leaveId, String decidedBy) {
        Leave leave = findLeaveOrThrow(leaveId);
        leave.approve(decidedBy);
        LeaveAllowance allowance = findAllowanceOrThrow(leave.getEmployeeId());
        allowance.deduct(leave.getDurationInDays());
        leaveAllowanceRepository.save(allowance);
        LeaveDto dto = leaveMapper.toDto(leaveRepository.save(leave));
        eventPublisher.publishEvent(new LeaveApprovedEvent(leave.getId(), leave.getEmployeeId(), leave.getDurationInDays()));
        return dto;
    }

    public LeaveDto rejectLeave(Long leaveId, String reason, String decidedBy) {
        Leave leave = findLeaveOrThrow(leaveId);
        leave.reject(new RejectionReason(reason), decidedBy);
        LeaveDto dto = leaveMapper.toDto(leaveRepository.save(leave));
        eventPublisher.publishEvent(new LeaveRejectedEvent(leave.getId(), leave.getEmployeeId()));
        return dto;
    }

    public LeaveDto amendLeave(Long leaveId, Long employeeId, AmendLeaveCommand command) {
        Leave leave = findLeaveOrThrow(leaveId);
        if (!leave.getEmployeeId().equals(employeeId)) {
            throw new IllegalStateException("Only the owner can amend their leave");
        }
        leave.amend(command.getType(), command.getStartDate(), command.getEndDate(), command.getReason());
        return leaveMapper.toDto(leaveRepository.save(leave));
    }

    public LeaveDto cancelLeave(Long leaveId, Long employeeId) {
        Leave leave = findLeaveOrThrow(leaveId);
        boolean wasApproved = leave.getStatus().name().equals("APPROVED");
        leave.cancel(employeeId);
        if (wasApproved) {
            LeaveAllowance allowance = findAllowanceOrThrow(employeeId);
            allowance.restore(leave.getDurationInDays());
            leaveAllowanceRepository.save(allowance);
        }
        return leaveMapper.toDto(leaveRepository.save(leave));
    }

    private Leave findLeaveOrThrow(Long id) {
        return leaveRepository.findById(id)
                .orElseThrow(() -> new LeaveRequestNotFoundException(id));
    }

    private LeaveAllowance findAllowanceOrThrow(Long employeeId) {
        int year = LocalDate.now().getYear();
        return leaveAllowanceRepository.findByEmployeeIdAndYear(employeeId, year)
                .orElseThrow(() -> new LeaveAllowanceNotFoundException(employeeId, year));
    }

    public LeaveAllowanceDto amendAllowance(Long employeeId, AmendAllowanceCommand command) {
        int year = LocalDate.now().getYear();
        LeaveAllowance allowance = leaveAllowanceRepository
                .findByEmployeeIdAndYear(employeeId, year)
                .orElse(new LeaveAllowance(employeeId, year, command.getTotalDays()));
        allowance.amendTotalDays(command.getTotalDays());
        return leaveAllowanceMapper.toDto(leaveAllowanceRepository.save(allowance));
    }
}