package com.example.productivity_app.controller;

import com.example.productivity_app.dto.CreateTaskResponseDTO;
import com.example.productivity_app.dto.TaskDTO;
import com.example.productivity_app.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;

@RestController
@RequestMapping("task")
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
}
