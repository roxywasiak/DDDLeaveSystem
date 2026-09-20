package com.example.leave.infrastructure.notification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaveNotificationRepository extends JpaRepository<LeaveNotification, Long> {
    List<LeaveNotification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);
}
