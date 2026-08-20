package com.example.leave.infrastructure.eventstore;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventStoreService {

    private final EventStoreRepository eventStoreRepository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void append(Object event, String status) {
        try {
            EventStore entry = new EventStore();
            entry.setEventType(event.getClass().getSimpleName());
            entry.setEventBody(objectMapper.writeValueAsString(event));
            entry.setOccurredOn(LocalDateTime.now());
            entry.setStatus(status);
            eventStoreRepository.save(entry);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialise event {} to event store", event.getClass().getSimpleName(), e);
        }
    }
}
