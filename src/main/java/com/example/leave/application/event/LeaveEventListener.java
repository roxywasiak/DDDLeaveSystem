package com.example.leave.application.event;

import com.example.leave.domain.event.LeaveApprovedEvent;
import com.example.leave.domain.event.LeaveRejectedEvent;
import com.example.leave.infrastructure.eventstore.EventStoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class LeaveEventListener {

    private final EventStoreService eventStoreService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLeaveApproved(LeaveApprovedEvent event) {
        log.info("Leave APPROVED — leaveId={}, employeeId={}, days={}",
                event.leaveId(), event.employeeId(), event.durationInDays());
        eventStoreService.append(event, "PUBLISHED");
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLeaveRejected(LeaveRejectedEvent event) {
        log.info("Leave REJECTED — leaveId={}, employeeId={}",
                event.leaveId(), event.employeeId());
        eventStoreService.append(event, "PUBLISHED");
    }
}
