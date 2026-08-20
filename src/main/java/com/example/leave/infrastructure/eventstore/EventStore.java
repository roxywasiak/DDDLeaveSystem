package com.example.leave.infrastructure.eventstore;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "event_store")
@Getter
@Setter
public class EventStore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false, length = 65000)
    private String eventBody;

    @Column(nullable = false)
    private LocalDateTime occurredOn;

    @Column(nullable = false)
    private String status;
}
