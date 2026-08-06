package com.example.leave.domain;

import com.example.leave.domain.model.Leave;
import com.example.leave.domain.model.LeaveStatus;
import com.example.leave.domain.model.LeaveType;
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
        leave.approve();
        assertEquals(LeaveStatus.APPROVED, leave.getStatus());
    }

    @Test
    void shouldRejectLeave() {
        Leave leave = createPendingLeave();
        leave.reject();
        assertEquals(LeaveStatus.REJECTED, leave.getStatus());
    }

    @Test
    void shouldNotApproveNonPendingLeave() {
        Leave leave = createPendingLeave();
        leave.approve();
        assertThrows(IllegalStateException.class, leave::approve);
    }

    @Test
    void shouldNotRejectNonPendingLeave() {
        Leave leave = createPendingLeave();
        leave.reject();
        assertThrows(IllegalStateException.class, leave::reject);
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
