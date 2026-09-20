package com.example.leave.interfaces.rest;

import com.example.leave.application.exception.EmployeeNotFoundException;
import com.example.leave.domain.repository.EmployeeRepository;
import com.example.leave.infrastructure.notification.LeaveNotification;
import com.example.leave.infrastructure.notification.LeaveNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final LeaveNotificationRepository notificationRepository;
    private final EmployeeRepository employeeRepository;

    @GetMapping
    public ResponseEntity<List<LeaveNotification>> getMyNotifications(Authentication auth) {
        Long employeeId = resolveId(auth);
        return ResponseEntity.ok(notificationRepository.findByRecipientIdOrderByCreatedAtDesc(employeeId));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id, Authentication auth) {
        Long employeeId = resolveId(auth);
        notificationRepository.findById(id).ifPresent(n -> {
            if (n.getRecipientId().equals(employeeId)) {
                n.markRead();
                notificationRepository.save(n);
            }
        });
        return ResponseEntity.noContent().build();
    }

    private Long resolveId(Authentication auth) {
        return employeeRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new EmployeeNotFoundException(auth.getName()))
                .getId();
    }
}
