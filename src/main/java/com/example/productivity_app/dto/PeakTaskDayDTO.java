package com.example.productivity_app.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PeakTaskDayDTO {
    private LocalDate date;
    private long taskCount;

    public PeakTaskDayDTO(LocalDate date, long taskCount) {
        this.date = date;
        this.taskCount = taskCount;
    }

    public LocalDate getDate() {
        return date;
    }

    public long getTaskCount() {
        return taskCount;
    }
}
