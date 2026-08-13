package com.example.leave.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "leave_allowances")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LeaveAllowance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long employeeId;

    @Column(name = "allowance_year", nullable = false)
    private int year;

    @Column(nullable = false)
    private int totalDays;

    @Column(nullable = false)
    private int usedDays;

    public LeaveAllowance(Long employeeId, int year, int totalDays) {
        if (employeeId == null) throw new IllegalArgumentException("Employee ID is required");
        if (totalDays <= 0) throw new IllegalArgumentException("Total days must be positive");
        this.employeeId = employeeId;
        this.year = year;
        this.totalDays = totalDays;
        this.usedDays = 0;
    }

    public int getRemainingDays() {
        return totalDays - usedDays;
    }

    public void deduct(int days) {
        if (days <= 0) throw new IllegalArgumentException("Days to deduct must be positive");
        if (days > getRemainingDays()) throw new IllegalStateException("Insufficient leave balance");
        this.usedDays += days;
    }

    public void restore(int days) {
        if (days <= 0) throw new IllegalArgumentException("Days to restore must be positive");
        this.usedDays = Math.max(0, this.usedDays - days);
    }

    public void amendTotalDays(int newTotal) {
        if (newTotal <= 0) throw new IllegalArgumentException("Total days must be positive");
        this.totalDays = newTotal;
    }
}
