package com.example.leave.domain.repository;

import com.example.leave.domain.model.LeaveAllowance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LeaveAllowanceRepository extends JpaRepository<LeaveAllowance, Long> {
    Optional<LeaveAllowance> findByEmployeeIdAndYear(Long employeeId, int year);
    List<LeaveAllowance> findByEmployeeIdOrderByYearDesc(Long employeeId);
}
