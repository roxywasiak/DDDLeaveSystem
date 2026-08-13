package com.example.leave.application.mapper;

import com.example.leave.application.dto.LeaveAllowanceDto;
import com.example.leave.domain.model.LeaveAllowance;
import org.springframework.stereotype.Component;

@Component
public class LeaveAllowanceMapper {

    public LeaveAllowanceDto toDto(LeaveAllowance allowance) {
        LeaveAllowanceDto dto = new LeaveAllowanceDto();
        dto.setId(allowance.getId());
        dto.setEmployeeId(allowance.getEmployeeId());
        dto.setYear(allowance.getYear());
        dto.setTotalDays(allowance.getTotalDays());
        dto.setUsedDays(allowance.getUsedDays());
        dto.setRemainingDays(allowance.getRemainingDays());
        return dto;
    }
}
