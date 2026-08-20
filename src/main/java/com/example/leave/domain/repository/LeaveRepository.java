package com.example.leave.domain.repository;

import com.example.leave.domain.model.Leave;
import com.example.leave.domain.model.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LeaveRepository extends JpaRepository<Leave, Long> {
    List<Leave> findByEmployeeId(Long employeeId);
    List<Leave> findByStatus(LeaveStatus status);
    List<Leave> findByStatusAndEmployeeIdIn(LeaveStatus status, List<Long> employeeIds);

    @Query("SELECT l.employeeId, SUM(DATEDIFF(l.endDate, l.startDate) + 1) " +
           "FROM Leave l " +
           "WHERE l.status = 'APPROVED' " +
           "AND l.employeeId IN :employeeIds " +
           "AND YEAR(l.startDate) = :year " +
           "GROUP BY l.employeeId")
    List<Object[]> sumApprovedDaysByEmployeeIdInAndYear(
            @Param("employeeIds") List<Long> employeeIds,
            @Param("year") int year);
}
