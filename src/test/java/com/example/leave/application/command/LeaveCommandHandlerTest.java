package com.example.leave.application.command;

import com.example.leave.application.dto.CreateLeaveCommand;
import com.example.leave.application.dto.LeaveDto;
import com.example.leave.application.exception.LeaveRequestNotFoundException;
import com.example.leave.application.mapper.LeaveAllowanceMapper;
import com.example.leave.application.mapper.LeaveMapper;
import com.example.leave.domain.model.Leave;
import com.example.leave.domain.model.LeaveAllowance;
import com.example.leave.domain.model.LeaveStatus;
import com.example.leave.domain.model.LeaveType;
import com.example.leave.domain.repository.LeaveAllowanceRepository;
import com.example.leave.domain.repository.LeaveRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveCommandHandlerTest {

    @Mock private LeaveRepository leaveRepository;
    @Mock private LeaveAllowanceRepository leaveAllowanceRepository;
    @Mock private LeaveMapper leaveMapper;
    @Mock private LeaveAllowanceMapper leaveAllowanceMapper;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private LeaveCommandHandler handler;

    private LeaveDto dto(LeaveStatus status) {
        return new LeaveDto(1L, 1L, LeaveType.ANNUAL,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5),
                "Holiday", status, null, null, null, null);
    }

    @Test
    void shouldCreateLeave() {
        CreateLeaveCommand command = new CreateLeaveCommand(LeaveType.ANNUAL,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5), "Holiday");

        when(leaveRepository.save(any(Leave.class))).thenAnswer(i -> i.getArgument(0));
        when(leaveMapper.toDto(any(Leave.class))).thenReturn(dto(LeaveStatus.PENDING));

        LeaveDto result = handler.createLeave(1L, command);

        assertEquals(LeaveStatus.PENDING, result.getStatus());
        verify(leaveRepository).save(any(Leave.class));
    }

    @Test
    void shouldApproveLeave() {
        Leave leave = new Leave(1L, LeaveType.ANNUAL,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5), "Holiday");
        LeaveAllowance allowance = new LeaveAllowance(1L, LocalDate.now().getYear(), 25);

        when(leaveRepository.findById(1L)).thenReturn(Optional.of(leave));
        when(leaveAllowanceRepository.findByEmployeeIdAndYear(eq(1L), anyInt())).thenReturn(Optional.of(allowance));
        when(leaveRepository.save(any(Leave.class))).thenAnswer(i -> i.getArgument(0));
        when(leaveAllowanceRepository.save(any(LeaveAllowance.class))).thenAnswer(i -> i.getArgument(0));
        when(leaveMapper.toDto(any(Leave.class))).thenReturn(dto(LeaveStatus.APPROVED));

        LeaveDto result = handler.approveLeave(1L, "manager@example.com");

        assertEquals(LeaveStatus.APPROVED, result.getStatus());
    }

    @Test
    void shouldThrowWhenLeaveNotFound() {
        when(leaveRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(LeaveRequestNotFoundException.class, () -> handler.approveLeave(99L, "manager@example.com"));
    }

    @Test
    void shouldRejectLeave() {
        Leave leave = new Leave(1L, LeaveType.ANNUAL,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5), "Holiday");

        when(leaveRepository.findById(1L)).thenReturn(Optional.of(leave));
        when(leaveRepository.save(any(Leave.class))).thenAnswer(i -> i.getArgument(0));
        when(leaveMapper.toDto(any(Leave.class))).thenReturn(dto(LeaveStatus.REJECTED));

        LeaveDto result = handler.rejectLeave(1L, "Not enough cover", "manager@example.com");

        assertEquals(LeaveStatus.REJECTED, result.getStatus());
    }

    @Test
    void shouldCancelLeave() {
        Leave leave = new Leave(1L, LeaveType.ANNUAL,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5), "Holiday");

        when(leaveRepository.findById(1L)).thenReturn(Optional.of(leave));
        when(leaveRepository.save(any(Leave.class))).thenAnswer(i -> i.getArgument(0));
        when(leaveMapper.toDto(any(Leave.class))).thenReturn(dto(LeaveStatus.CANCELLED));

        LeaveDto result = handler.cancelLeave(1L, 1L);

        assertEquals(LeaveStatus.CANCELLED, result.getStatus());
    }
}
