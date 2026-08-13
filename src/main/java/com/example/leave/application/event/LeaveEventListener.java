package com.example.leave.application.event;

import com.example.leave.domain.event.LeaveApprovedEvent;
import com.example.leave.domain.event.LeaveRejectedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LeaveEventListener {

    @EventListener
    public void onLeaveApproved(LeaveApprovedEvent event) {
        log.info("Leave APPROVED — leaveId={}, employeeId={}, days={}",
                event.leaveId(), event.employeeId(), event.durationInDays());
    }

    @EventListener
    public void onLeaveRejected(LeaveRejectedEvent event) {
        log.info("Leave REJECTED — leaveId={}, employeeId={}",
                event.leaveId(), event.employeeId());
    }
}
