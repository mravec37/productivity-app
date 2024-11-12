package com.example.productivity_app.service;

import com.example.productivity_app.entity.Task;
import com.example.productivity_app.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    public Task createTask(String taskName, LocalTime startTime, LocalTime endTime,
                           LocalDate startDate, LocalDate endDate, String taskDescription) {
        Task task = new Task();
        task.setTaskName(taskName);
        task.setStartTime(startTime);
        task.setEndTime(endTime);
        task.setStartDate(startDate);
        task.setEndDate(endDate);
        task.setTaskDescription(taskDescription);
        taskRepository.save(task);

        return task;
    }

    public List<Task> getTasksByDate(LocalDate date) {
        return taskRepository.findByStartDate(date);
    }
}
