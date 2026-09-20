package com.example.leave.application.mapper;

import com.example.leave.application.dto.EmployeeDto;
import com.example.leave.domain.model.Employee;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmployeeMapper {

    public EmployeeDto toDto(Employee employee) {
        return new EmployeeDto(
                employee.getId(),
                employee.getEmail(),
                employee.getName(),
                employee.getRole(),
                employee.getManagerId(),
                employee.getDepartment()
        );
    }

    public List<EmployeeDto> toDtoList(List<Employee> employees) {
        return employees.stream().map(this::toDto).toList();
    }
}
