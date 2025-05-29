package com.example.productivity_app.service;

import com.example.productivity_app.dto.PeakTaskDayDTO;
import com.example.productivity_app.entity.Task;
import com.example.productivity_app.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    public List<Task> getTasksByDate(LocalDate startDate, LocalDate endDate) {
        return taskRepository.findTasksByStartOrEndTimeInRange(startDate, endDate);
    }

    public boolean createTaskIfNoOverlap(String taskName, LocalTime startTime, LocalTime endTime, LocalDate startDate, LocalDate endDate, String taskDescription, String taskColor, String username) {
        int result = taskRepository.createTaskIfNoOverlap(
                taskName, startTime, endTime, startDate, endDate, taskDescription, taskColor, username
        );

        // If the repository returns 1, the task was created successfully
        return result > 0;
    }

    public boolean updateTaskIfNoOverlap(Long id, String taskName, LocalTime startTime, LocalTime endTime, LocalDate startDate, LocalDate endDate, String taskDescription, String taskColor) {
        int result = taskRepository.updateTaskIfNoOverlap(
                id, taskName, startTime, endTime, startDate, endDate, taskDescription, taskColor
        );

        // If the repository returns 1, the task was created successfully
        return result > 0;
    }

    public Optional<Task> findByStartDateAndStartTime(LocalDate startDate, LocalTime startTime, String userName) {
       return taskRepository.findByUserStartDateAndStartTime(startDate, startTime, userName);
    }

    public void deleteById(Long id) {
        taskRepository.deleteById(id);
    }

    public List<Task> getTasksByDateForUser(String userName, LocalDate startDate, LocalDate endDate) {
        return taskRepository.findTasksByUsernameAndStartOrEndTimeInRange(userName, startDate, endDate);
    }


    public int countUserDoneTasks(long userId, LocalDate nowDate, LocalTime nowTime) {
        return taskRepository.countDoneTasks(userId, nowDate, nowTime);
    }

    public int countUserPlannedTasks(long userId, LocalDate nowDate, LocalTime nowTime) {
        return taskRepository.countPlannedTasks(userId, nowDate, nowTime);
    }

    public int countTotalTaskTimeInMinutes(long id, LocalDate nowDate, LocalTime nowTime) {
        Integer totalTaskTime = taskRepository.countTotalTaskTimeInMinutes(id, nowDate, nowTime);
        if(totalTaskTime == null) return 0;
        return totalTaskTime;
    }

    public Optional<Task> getLongestTask(long id) {
        return taskRepository.getLongestTask(id);
    }

    public double calculateTaskLength(Task longestTask) {
        LocalDateTime startDateTime = LocalDateTime.of(longestTask.getStartDate(), longestTask.getStartTime());
        LocalDateTime endDateTime = LocalDateTime.of(longestTask.getEndDate(), longestTask.getEndTime());

        long minutes = Duration.between(startDateTime, endDateTime).toMinutes();
        double hours = minutes / 60.0;

        // Round to 1 decimal place
        return Math.round(hours * 10.0) / 10.0;
    }

    public PeakTaskDayDTO getUserPeakTaskDay(Long userId) {
        Map<String, Object> result = taskRepository.findPeakTaskDayForUser(userId);
        if (result == null) {
            throw new RuntimeException("No tasks found for this user.");
        }
        LocalDate date = LocalDate.parse(result.get("date").toString());
        long taskCount = ((Number) result.get("task_count")).longValue();
        return new PeakTaskDayDTO(date, taskCount);
    }

}
