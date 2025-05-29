package com.example.productivity_app.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class GetLongestTaskDTO {
    double durationHours;
    LocalDate startDate;
    LocalDate endDate;
    String taskName;

    public GetLongestTaskDTO(double durationHours, LocalDate startDate, LocalDate endDate, String taskName) {
        this.durationHours = durationHours;
        this.startDate = startDate;
        this.endDate = endDate;
        this.taskName = taskName;
    }
}
