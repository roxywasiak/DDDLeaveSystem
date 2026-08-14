package com.example.leave.domain.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "leave_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Leave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long employeeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveType type;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    private String reason;

    @Embedded
    private RejectionReason rejectionReason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveStatus status;

    public Leave(Long employeeId, LeaveType type, LocalDate startDate, LocalDate endDate, String reason) {
        if (employeeId == null) {
            throw new IllegalArgumentException("Employee ID is required");
        }
        if (type == null) {
            throw new IllegalArgumentException("Leave type is required");
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start and end dates are required");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
        if (reason != null && reason.length() > 500) {
            throw new IllegalArgumentException("Reason must not exceed 500 characters");
        }
        this.employeeId = employeeId;
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.status = LeaveStatus.PENDING;
    }

    public void approve() {
        if (this.status != LeaveStatus.PENDING) {
            throw new IllegalStateException("Only pending leave can be approved");
        }
        this.status = LeaveStatus.APPROVED;
    }

    public void reject(RejectionReason rejectionReason) {
        if (this.status != LeaveStatus.PENDING) {
            throw new IllegalStateException("Only pending leave can be rejected");
        }
        if (rejectionReason == null) {
            throw new IllegalArgumentException("Rejection reason is required");
        }
        this.rejectionReason = rejectionReason;
        this.status = LeaveStatus.REJECTED;
    }

    public void cancel(Long requestingEmployeeId) {
        if (!this.employeeId.equals(requestingEmployeeId)) {
            throw new IllegalStateException("Only the owner can cancel their leave");
        }
        if (this.status == LeaveStatus.CANCELLED) {
            throw new IllegalStateException("Leave is already cancelled");
        }
        this.status = LeaveStatus.CANCELLED;
    }

    public int getDurationInDays() {
        return (int) (endDate.toEpochDay() - startDate.toEpochDay()) + 1;
    }
}
