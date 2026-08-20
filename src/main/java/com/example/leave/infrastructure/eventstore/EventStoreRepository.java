package com.example.leave.infrastructure.eventstore;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EventStoreRepository extends JpaRepository<EventStore, Long> {
}
