package com.example.leave.domain;

import com.example.leave.domain.model.Leave;
import com.example.leave.domain.model.LeaveStatus;
import com.example.leave.domain.model.LeaveType;
import com.example.leave.domain.model.RejectionReason;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class LeaveTest {

    @Test
    void shouldCreateLeaveWithPendingStatus() {
        Leave leave = new Leave(1L, LeaveType.ANNUAL,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5), "Holiday");

        assertEquals(LeaveStatus.PENDING, leave.getStatus());
        assertEquals(LeaveType.ANNUAL, leave.getType());
        assertEquals(1L, leave.getEmployeeId());
    }

    @Test
    void shouldThrowWhenEndDateBeforeStartDate() {
        assertThrows(IllegalArgumentException.class, () ->
                new Leave(1L, LeaveType.ANNUAL,
                        LocalDate.now().plusDays(5), LocalDate.now().plusDays(1), "Invalid"));
    }

    @Test
    void shouldThrowWhenEmployeeIdIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new Leave(null, LeaveType.ANNUAL,
                        LocalDate.now().plusDays(1), LocalDate.now().plusDays(5), "Holiday"));
    }

    @Test
    void shouldThrowWhenTypeIsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                new Leave(1L, null,
                        LocalDate.now().plusDays(1), LocalDate.now().plusDays(5), "Holiday"));
    }

    @Test
    void shouldApproveLeave() {
        Leave leave = createPendingLeave();
        leave.approve("manager@example.com");
        assertEquals(LeaveStatus.APPROVED, leave.getStatus());
        assertNotNull(leave.getDecidedAt());
        assertEquals("manager@example.com", leave.getDecidedBy());
    }

    @Test
    void shouldRejectLeave() {
        Leave leave = createPendingLeave();
        leave.reject(new RejectionReason("Not enough cover"), "manager@example.com");
        assertEquals(LeaveStatus.REJECTED, leave.getStatus());
        assertEquals("Not enough cover", leave.getRejectionReason().getValue());
        assertNotNull(leave.getDecidedAt());
    }

    @Test
    void shouldNotApproveNonPendingLeave() {
        Leave leave = createPendingLeave();
        leave.approve("manager@example.com");
        assertThrows(IllegalStateException.class, () -> leave.approve("manager@example.com"));
    }

    @Test
    void shouldNotRejectNonPendingLeave() {
        Leave leave = createPendingLeave();
        leave.reject(new RejectionReason("Not enough cover"), "manager@example.com");
        assertThrows(IllegalStateException.class, () -> leave.reject(new RejectionReason("Again"), "manager@example.com"));
    }

    @Test
    void shouldAmendPendingLeave() {
        Leave leave = createPendingLeave();
        LocalDate newStart = LocalDate.now().plusDays(10);
        LocalDate newEnd = LocalDate.now().plusDays(15);
        leave.amend(LeaveType.SICK, newStart, newEnd, "Changed");
        assertEquals(LeaveType.SICK, leave.getType());
        assertEquals(newStart, leave.getStartDate());
        assertEquals(newEnd, leave.getEndDate());
    }

    @Test
    void shouldNotAmendNonPendingLeave() {
        Leave leave = createPendingLeave();
        leave.approve("manager@example.com");
        assertThrows(IllegalStateException.class, () ->
                leave.amend(LeaveType.SICK, LocalDate.now().plusDays(1), LocalDate.now().plusDays(3), null));
    }

    @Test
    void shouldCancelOwnLeave() {
        Leave leave = createPendingLeave();
        leave.cancel(1L);
        assertEquals(LeaveStatus.CANCELLED, leave.getStatus());
    }

    @Test
    void shouldNotCancelOtherEmployeesLeave() {
        Leave leave = createPendingLeave();
        assertThrows(IllegalStateException.class, () -> leave.cancel(999L));
    }

    @Test
    void shouldNotCancelAlreadyCancelledLeave() {
        Leave leave = createPendingLeave();
        leave.cancel(1L);
        assertThrows(IllegalStateException.class, () -> leave.cancel(1L));
    }

    private Leave createPendingLeave() {
        return new Leave(1L, LeaveType.ANNUAL,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5), "Holiday");
    }
}
