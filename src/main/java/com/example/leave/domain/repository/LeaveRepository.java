package com.example.leave.domain.repository;

import com.example.leave.domain.model.Leave;
import com.example.leave.domain.model.LeaveStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LeaveRepository extends JpaRepository<Leave, Long> {
    List<Leave> findByEmployeeId(Long employeeId);
    List<Leave> findByStatus(LeaveStatus status);
    Page<Leave> findByStatusAndEmployeeIdIn(LeaveStatus status, List<Long> employeeIds, Pageable pageable);

    @Query("SELECT l FROM Leave l " +
           "WHERE l.status = com.example.leave.domain.model.LeaveStatus.APPROVED " +
           "AND l.employeeId IN :employeeIds " +
           "AND FUNCTION('YEAR', l.startDate) = :year")
    List<Leave> findApprovedByEmployeeIdInAndYear(
            @Param("employeeIds") List<Long> employeeIds,
            @Param("year") int year);
}
