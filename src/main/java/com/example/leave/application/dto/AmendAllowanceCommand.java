package com.example.leave.application.dto;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AmendAllowanceCommand {

    @Min(1)
    private int totalDays;
}
