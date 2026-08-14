package com.example.leave.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RejectionReason {

    @Column(name = "rejection_reason", length = 500)
    private String value;

    public RejectionReason(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Rejection reason must not be blank");
        }
        if (value.length() > 500) {
            throw new IllegalArgumentException("Rejection reason must not exceed 500 characters");
        }
        this.value = value.trim();
    }

    @Override
    public String toString() {
        return value;
    }
}
