package com.example.productivity_app.controller;

import com.example.productivity_app.dto.*;
import com.example.productivity_app.entity.Task;
import com.example.productivity_app.entity.User;
import com.example.productivity_app.repository.TaskRepository;
import com.example.productivity_app.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@RestController
@RequestMapping("task")
public class TaskController {

    @Autowired
    private TaskService taskService;
    @Autowired
    private TaskRepository taskRepository;


    @PostMapping("/createTask")
    public CreateTaskResponseDTO createTask(@RequestBody TaskDTO taskDTO) {

        String userName = SecurityContextHolder.getContext().getAuthentication().getName();

        boolean isTaskCreated = taskService.createTaskIfNoOverlap(taskDTO.getTaskName(), taskDTO.getStartTime(), taskDTO.getEndTime(),
                taskDTO.getStartDate(), taskDTO.getEndDate(), taskDTO.getTaskDescription(), taskDTO.getTaskColor(), userName);

        CreateTaskResponseDTO taskResponseDTO = new CreateTaskResponseDTO();
        if(isTaskCreated) {
            Optional<Task> newTask = taskService.findByStartDateAndStartTime(taskDTO.getStartDate(), taskDTO.getStartTime(), userName);
            newTask.ifPresent(taskResponseDTO::setTask);
        }

        taskResponseDTO.setMessage(isTaskCreated ? "Success" : "Failure");
        return taskResponseDTO;
    }

    @DeleteMapping("/deleteTask")
    public ResponseDTO deleteTask(@RequestBody TaskDeleteDTO taskDTO) {
        System.out.println("Deleting !!!!");
        System.out.println("Id: " + taskDTO.getId());

        taskService.deleteById(taskDTO.getId());

        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.setMessage("Success");
        return responseDTO;
    }

    @GetMapping("/getTasks")
    public GetTasksResponseDTO getTasksByDate(
            @RequestParam("startDate") LocalDate startDate,
            @RequestParam("endDate") LocalDate endDate) {

        String userName = SecurityContextHolder.getContext().getAuthentication().getName();

        GetTasksResponseDTO tasksResponseDTO = new GetTasksResponseDTO();
        tasksResponseDTO.addTasks(taskService.getTasksByDateForUser(userName, startDate, endDate));

        tasksResponseDTO.getTasksList().forEach(task-> System.out.println("Task desc: " + task.getTaskName()));

        tasksResponseDTO.setMessage("Success");
        return tasksResponseDTO;
    }


    @PutMapping("/updateTask")
    public UpdateTaskDTO updateTask(@RequestBody TaskDTO taskDTO) {
        boolean isTaskUpdated = taskService.updateTaskIfNoOverlap(taskDTO.getId(), taskDTO.getTaskName(), taskDTO.getStartTime(), taskDTO.getEndTime(),
                taskDTO.getStartDate(), taskDTO.getEndDate(), taskDTO.getTaskDescription(), taskDTO.getTaskColor());

        UpdateTaskDTO updateTaskDTO = new UpdateTaskDTO();
        updateTaskDTO.setMessage(isTaskUpdated ? "Success" : "Failure");

        Task task = new Task();
        task.setTaskName(taskDTO.getTaskName());
        task.setId(taskDTO.getId());
        task.setTaskDescription(taskDTO.getTaskDescription());
        task.setStartDate(taskDTO.getStartDate());
        task.setEndDate(taskDTO.getEndDate());
        task.setStartTime(taskDTO.getStartTime());
        task.setEndTime(taskDTO.getEndTime());
        task.setTaskColor(taskDTO.getTaskColor());
        updateTaskDTO.setTask(task);

        return updateTaskDTO;
    }


    @GetMapping("/doneAndPlannedTasks")
    public ResponseEntity<GetUserDoneAndPlannedTaskInfoDTO> getUserDoneAndPlannedTaskInfo() {
        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("done and planned tasks PlaceHolder 1");
            User currentUser = (User) authentication.getPrincipal();
            if (currentUser == null) {
                throw new RuntimeException("Invalid token");
            }
            long id = currentUser.getId();
            int userDoneTasks = taskService.countUserDoneTasks(id, LocalDate.now(), LocalTime.now());
            int userPlannedTasks = taskService.countUserPlannedTasks(id, LocalDate.now(), LocalTime.now());

            return ResponseEntity.ok(new GetUserDoneAndPlannedTaskInfoDTO(userDoneTasks, userPlannedTasks));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    @GetMapping("/getTotalTaskTime")
    public ResponseEntity<Double> getUserTotalTaskTime() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) authentication.getPrincipal();
            if (currentUser == null) {
                throw new RuntimeException("Invalid token");
            }
            long id = currentUser.getId();
            double totalTaskTime = taskService.countTotalTaskTimeInMinutes(id, LocalDate.now(), LocalTime.now());
            System.out.println("Total task time: " + totalTaskTime);
            totalTaskTime /= 60;
            double rounded = Math.round(totalTaskTime * 10.0) / 10.0;
            System.out.println("Pocet hodin: " + rounded);
            return ResponseEntity.ok(rounded);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/getLongestTask")
    public ResponseEntity<GetLongestTaskDTO> getUserLongestTask() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            System.out.println(" Longest task PlaceHolder 1");
            User currentUser = (User) authentication.getPrincipal();
            if (currentUser == null) {
                throw new RuntimeException("Invalid token");
            }
            long id = currentUser.getId();
            Optional<Task> taskOptional = taskService.getLongestTask(id);
            if (taskOptional.isEmpty()) return ResponseEntity.ok(new GetLongestTaskDTO(0,null, null, null));

            Task longestTask = taskOptional.get();
            double taskLength = taskService.calculateTaskLength(longestTask);
            System.out.println("Task lenght:" + taskLength + " TaskName: " + longestTask.getTaskName());
            return ResponseEntity.ok(new GetLongestTaskDTO(taskLength,longestTask.getStartDate(),
                    longestTask.getEndDate(), longestTask.getTaskName()));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/peakTaskDay")
    public ResponseEntity<PeakTaskDayDTO> getPeakTaskDay() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        PeakTaskDayDTO dto = taskService.getUserPeakTaskDay(currentUser.getId());
        return ResponseEntity.ok(dto);
    }


}
