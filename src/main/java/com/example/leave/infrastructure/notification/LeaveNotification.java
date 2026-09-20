package com.example.leave.infrastructure.notification;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "leave_notifications")
@Getter
@NoArgsConstructor
public class LeaveNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long recipientId;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private boolean read = false;

    @Column(nullable = false)
    private Instant createdAt;

    public LeaveNotification(Long recipientId, String message) {
        this.recipientId = recipientId;
        this.message = message;
        this.createdAt = Instant.now();
    }

    public void markRead() {
        this.read = true;
    }
}
