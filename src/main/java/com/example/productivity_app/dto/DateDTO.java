package com.example.productivity_app.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class DateDTO {
    private LocalDate startDate;
    private LocalDate endDate;
}
