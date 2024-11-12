package com.example.productivity_app.controller;

import com.example.productivity_app.dto.CreateTaskResponseDTO;
import com.example.productivity_app.dto.DateDTO;
import com.example.productivity_app.dto.GetTasksResponseDTO;
import com.example.productivity_app.dto.TaskDTO;
import com.example.productivity_app.entity.Task;
import com.example.productivity_app.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("task")
@CrossOrigin(origins = "http://192.168.1.36:8080")
public class TaskController {

    @Autowired
    private TaskService taskService;


    /*@PostMapping("/createTask")
    public CreateTaskResponseDTO createTask(@RequestParam(required = false) String taskName,
                                            @RequestParam(required = false) LocalTime startTime,
                                            @RequestParam(required = false) LocalTime endTime,
                                            @RequestParam(required = false) LocalDate startDate,
                                            @RequestParam(required = false) LocalDate endDate,
                                            @RequestParam(required = false) String taskDescription) {

        taskService.createTask(taskName, startTime, endTime, startDate, endDate, taskDescription);
        CreateTaskResponseDTO taskResponseDTO = new CreateTaskResponseDTO();
        taskResponseDTO.setMessage("Success");
        return taskResponseDTO;

    }*/

    @PostMapping("/createTask")
    public CreateTaskResponseDTO createTask(@RequestBody TaskDTO taskDTO) {

       taskService.createTask(taskDTO.getTaskName(), taskDTO.getStartTime(), taskDTO.getEndTime(),
                taskDTO.getStartDate(), taskDTO.getEndDate(), taskDTO.getTaskDescription());

        CreateTaskResponseDTO taskResponseDTO = new CreateTaskResponseDTO();
        taskResponseDTO.setMessage("Success");
        return taskResponseDTO;
    }

    @GetMapping("/getTasks")
    public GetTasksResponseDTO getTasksByDate(
            @RequestParam("startDate") LocalDate startDate,
            @RequestParam("endDate") LocalDate endDate) {

        System.out.println("Start date and end date: " + startDate + " " + endDate);
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        GetTasksResponseDTO tasksResponseDTO = new GetTasksResponseDTO();

        if(daysBetween > -1) {
            tasksResponseDTO.addTasks(taskService.getTasksByDate(startDate));
            for(int i = 1; i < daysBetween + 1; i++) {
                LocalDate startDatePlusIDays = startDate.plusDays(i);
                List<Task> tasksByDate = taskService.getTasksByDate(startDatePlusIDays);
                tasksResponseDTO.addTasks(tasksByDate);
            }
        }

        for(List<Task> tasks: tasksResponseDTO.getTasksList()) {
            for (Task task: tasks) {
                System.out.println("Task name: " + task.getTaskName());
            }
        }

        tasksResponseDTO.setMessage("Success");
        return tasksResponseDTO;
    }

}
