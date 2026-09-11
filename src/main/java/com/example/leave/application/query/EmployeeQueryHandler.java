package com.example.leave.application.query;

import com.example.leave.application.dto.EmployeeDto;
import com.example.leave.application.exception.EmployeeNotFoundException;
import com.example.leave.application.mapper.EmployeeMapper;
import com.example.leave.domain.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeQueryHandler {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    public Page<EmployeeDto> getAllEmployees(Pageable pageable) {
        return employeeRepository.findAll(pageable).map(employeeMapper::toDto);
    }

    public EmployeeDto getEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .map(employeeMapper::toDto)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
    }

    public EmployeeDto getEmployeeByEmail(String email) {
        return employeeRepository.findByEmail(email)
                .map(employeeMapper::toDto)
                .orElseThrow(() -> new EmployeeNotFoundException(email));
    }
}
