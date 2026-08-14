package com.example.leave.application.query;

import com.example.leave.application.dto.LeaveDto;
import com.example.leave.application.exception.LeaveRequestNotFoundException;
import com.example.leave.application.mapper.LeaveMapper;
import com.example.leave.domain.model.Leave;
import com.example.leave.domain.model.LeaveStatus;
import com.example.leave.domain.model.LeaveType;
import com.example.leave.domain.repository.LeaveRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveQueryHandlerTest {

    @Mock private LeaveRepository leaveRepository;
    @Mock private LeaveMapper leaveMapper;

    @InjectMocks
    private LeaveQueryHandler handler;

    private LeaveDto dto(LeaveType type, LeaveStatus status, String reason) {
        return new LeaveDto(1L, 1L, type,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5),
                reason, status, null, null, null, null);
    }

    @Test
    void shouldGetLeaveById() {
        Leave leave = new Leave(1L, LeaveType.ANNUAL,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5), "Holiday");
        LeaveDto expected = dto(LeaveType.ANNUAL, LeaveStatus.PENDING, "Holiday");

        when(leaveRepository.findById(1L)).thenReturn(Optional.of(leave));
        when(leaveMapper.toDto(leave)).thenReturn(expected);

        LeaveDto result = handler.getLeaveById(1L);

        assertEquals(LeaveType.ANNUAL, result.getType());
        assertEquals(LeaveStatus.PENDING, result.getStatus());
    }

    @Test
    void shouldThrowWhenLeaveNotFound() {
        when(leaveRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(LeaveRequestNotFoundException.class, () -> handler.getLeaveById(99L));
    }

    @Test
    void shouldGetLeavesByEmployee() {
        Leave leave = new Leave(1L, LeaveType.SICK,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), "Flu");
        LeaveDto expected = dto(LeaveType.SICK, LeaveStatus.PENDING, "Flu");

        when(leaveRepository.findByEmployeeId(1L)).thenReturn(List.of(leave));
        when(leaveMapper.toDtoList(List.of(leave))).thenReturn(List.of(expected));

        List<LeaveDto> result = handler.getLeavesByEmployee(1L);

        assertEquals(1, result.size());
        assertEquals(LeaveType.SICK, result.get(0).getType());
    }

    @Test
    void shouldGetPendingLeaves() {
        Leave leave = new Leave(1L, LeaveType.ANNUAL,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5), "Holiday");
        LeaveDto expected = dto(LeaveType.ANNUAL, LeaveStatus.PENDING, "Holiday");

        when(leaveRepository.findByStatus(LeaveStatus.PENDING)).thenReturn(List.of(leave));
        when(leaveMapper.toDtoList(List.of(leave))).thenReturn(List.of(expected));

        List<LeaveDto> result = handler.getPendingLeaves();

        assertEquals(1, result.size());
        assertEquals(LeaveStatus.PENDING, result.get(0).getStatus());
    }
}
