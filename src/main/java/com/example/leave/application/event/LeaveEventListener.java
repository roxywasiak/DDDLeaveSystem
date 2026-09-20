package com.example.leave.application.event;

import com.example.leave.domain.event.LeaveApprovedEvent;
import com.example.leave.domain.event.LeaveCancelledEvent;
import com.example.leave.domain.event.LeaveRejectedEvent;
import com.example.leave.domain.repository.EmployeeRepository;
import com.example.leave.infrastructure.eventstore.EventStoreService;
import com.example.leave.infrastructure.notification.LeaveNotification;
import com.example.leave.infrastructure.notification.LeaveNotificationRepository;
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
    private final LeaveNotificationRepository notificationRepository;
    private final EmployeeRepository employeeRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLeaveApproved(LeaveApprovedEvent event) {
        log.info("Leave APPROVED — leaveId={}, employeeId={}, days={}",
                event.leaveId(), event.employeeId(), event.durationInDays());
        eventStoreService.append(event, "PUBLISHED");
        // Notify employee their leave was approved
        notificationRepository.save(new LeaveNotification(
                event.employeeId(),
                "Your leave request #" + event.leaveId() + " has been approved."
        ));
        // Notify manager a leave on their team was approved
        employeeRepository.findById(event.employeeId()).ifPresent(emp -> {
            if (emp.getManagerId() != null) {
                notificationRepository.save(new LeaveNotification(
                        emp.getManagerId(),
                        "Leave request #" + event.leaveId() + " for " + emp.getName() + " has been approved."
                ));
            }
        });
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLeaveRejected(LeaveRejectedEvent event) {
        log.info("Leave REJECTED — leaveId={}, employeeId={}", event.leaveId(), event.employeeId());
        eventStoreService.append(event, "PUBLISHED");
        // Notify employee their leave was rejected
        notificationRepository.save(new LeaveNotification(
                event.employeeId(),
                "Your leave request #" + event.leaveId() + " has been rejected."
        ));
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLeaveCancelled(LeaveCancelledEvent event) {
        log.info("Leave CANCELLED — leaveId={}, employeeId={}", event.leaveId(), event.employeeId());
        // Notify manager a team member cancelled their leave
        employeeRepository.findById(event.employeeId()).ifPresent(emp -> {
            if (emp.getManagerId() != null) {
                notificationRepository.save(new LeaveNotification(
                        emp.getManagerId(),
                        emp.getName() + " has cancelled leave request #" + event.leaveId() + "."
                ));
            }
        });
    }
}
