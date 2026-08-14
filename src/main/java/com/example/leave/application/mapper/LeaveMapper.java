package com.example.leave.application.mapper;

import com.example.leave.application.dto.LeaveDto;
import com.example.leave.domain.model.Leave;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LeaveMapper {

    public LeaveDto toDto(Leave leave) {
        return new LeaveDto(
                leave.getId(),
                leave.getEmployeeId(),
                leave.getType(),
                leave.getStartDate(),
                leave.getEndDate(),
                leave.getReason(),
                leave.getStatus(),
                leave.getRejectionReason() != null ? leave.getRejectionReason().getValue() : null
        );
    }

    public List<LeaveDto> toDtoList(List<Leave> leaves) {
        return leaves.stream().map(this::toDto).toList();
    }
}
