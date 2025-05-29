package com.example.productivity_app.dto;

public class GetUserDoneAndPlannedTaskInfoDTO {
    public int tasksDone;
    public int tasksPlanned;

    public GetUserDoneAndPlannedTaskInfoDTO(int tasksDone, int tasksPlanned) {
        this.tasksDone = tasksDone;
        this.tasksPlanned = tasksPlanned;
    }
}
