package com.example.productivity_app.controller;

import com.example.productivity_app.dto.*;
import com.example.productivity_app.entity.Task;
import com.example.productivity_app.entity.User;
import com.example.productivity_app.repository.TaskRepository;
import com.example.productivity_app.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

/**
 * Controller responsible for handling task-related operations for authenticated users.
 * Provides endpoints for creating, updating, deleting, and fetching tasks,
 * as well as for task statistics like total time of completed tasks, longest task, etc.
 */
@RestController
@RequestMapping("task")
public class TaskController {

    @Autowired
    private TaskService taskService;
    @Autowired
    private TaskRepository taskRepository;

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    /**
     * Creates a new task for the authenticated user,
     * ensuring no overlapping tasks exist for the given time range.
     *
     * @param taskDTO the details of the task to create
     * @return response DTO indicating success or failure of creating a new task
     */
    @PostMapping("/createTask")
    public CreateTaskResponseDTO createTask(@RequestBody TaskDTO taskDTO) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info("User '{}' is creating a task: {}", userName, taskDTO.getTaskName());

        boolean isTaskCreated = taskService.createTaskIfNoOverlap(
                taskDTO.getTaskName(),
                taskDTO.getStartTime(),
                taskDTO.getEndTime(),
                taskDTO.getStartDate(),
                taskDTO.getEndDate(),
                taskDTO.getTaskDescription(),
                taskDTO.getTaskColor(),
                userName
        );

        CreateTaskResponseDTO taskResponseDTO = new CreateTaskResponseDTO();
        if (isTaskCreated) {
            Optional<Task> newTask = taskService.findByStartDateAndStartTime(
                    taskDTO.getStartDate(),
                    taskDTO.getStartTime(),
                    userName
            );
            newTask.ifPresent(taskResponseDTO::setTask);
            logger.info("Task created successfully for user '{}': {}", userName, taskDTO.getTaskName());
        } else {
            logger.debug("Failed to create task due to overlapping times for user '{}': {}", userName, taskDTO.getTaskName());
        }

        taskResponseDTO.setMessage(isTaskCreated ? "Success" : "Failure");
        return taskResponseDTO;
    }


    @DeleteMapping("/deleteTask")
    public ResponseDTO deleteTask(@RequestBody TaskDeleteDTO taskDTO) {
        taskService.deleteById(taskDTO.getId());
        logger.info("Task with ID {} deleted", taskDTO.getId());

        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.setMessage("Success");
        return responseDTO;
    }

    /**
     * Retrieves tasks for the authenticated user between two dates.
     *
     * @param startDate the start date from which the tasks should be included
     * @param endDate the end date of task inclusion
     * @return response DTO containing the list of tasks
     */
    @GetMapping("/getTasks")
    public GetTasksResponseDTO getTasksByDate(
            @RequestParam("startDate") LocalDate startDate,
            @RequestParam("endDate") LocalDate endDate
    ) {
        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        logger.info("Fetching tasks for user '{}' between {} and {}", userName, startDate, endDate);

        GetTasksResponseDTO tasksResponseDTO = new GetTasksResponseDTO();
        tasksResponseDTO.addTasks(taskService.getTasksByDateForUser(userName, startDate, endDate));

        if (logger.isDebugEnabled()) {
            tasksResponseDTO.getTasksList()
                    .forEach(task -> logger.debug("Task fetched: {}", task.getTaskName()));
        }

        tasksResponseDTO.setMessage("Success");
        return tasksResponseDTO;
    }

    /**
     * Updates an existing task for the authenticated user,
     * ensuring the new time range does not overlap with other tasks.
     *
     * @param taskDTO the updated task details
     * @return response DTO indicating success or failure, including updated task details
     */
    @PutMapping("/updateTask")
    public UpdateTaskDTO updateTask(@RequestBody TaskDTO taskDTO) {
        boolean isTaskUpdated = taskService.updateTaskIfNoOverlap(
                taskDTO.getId(),
                taskDTO.getTaskName(),
                taskDTO.getStartTime(),
                taskDTO.getEndTime(),
                taskDTO.getStartDate(),
                taskDTO.getEndDate(),
                taskDTO.getTaskDescription(),
                taskDTO.getTaskColor()
        );

        UpdateTaskDTO updateTaskDTO = new UpdateTaskDTO();
        updateTaskDTO.setMessage(isTaskUpdated ? "Success" : "Failure");

        Task task = taskService.createAndFillTask(taskDTO);
        updateTaskDTO.setTask(task);

        if (isTaskUpdated) {
            logger.info("Task updated successfully: ID={}, Name={}", taskDTO.getId(), taskDTO.getTaskName());
        } else {
            logger.debug("Failed to update task (overlap issue?) ID={}, Name={}", taskDTO.getId(), taskDTO.getTaskName());
        }

        return updateTaskDTO;
    }


    /**
     * Returns the number of completed and planned tasks for the authenticated user
     *
     * @return DTO containing counts of done and planned tasks
     */
    @GetMapping("/doneAndPlannedTasks")
    public ResponseEntity<GetUserDoneAndPlannedTaskInfoDTO> getUserDoneAndPlannedTaskInfo() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) authentication.getPrincipal();

            if (currentUser == null) {
                throw new AuthenticationCredentialsNotFoundException("Invalid authentication or authentication missing");
            }

            long id = currentUser.getId();
            int userDoneTasks = taskService.countUserDoneTasks(id, LocalDate.now(), LocalTime.now());
            int userPlannedTasks = taskService.countUserPlannedTasks(id, LocalDate.now(), LocalTime.now());

            logger.info("User ID {} has {} done tasks and {} planned tasks.", id, userDoneTasks, userPlannedTasks);
            return ResponseEntity.ok(new GetUserDoneAndPlannedTaskInfoDTO(userDoneTasks, userPlannedTasks));

        } catch(AuthenticationCredentialsNotFoundException e) {
            logger.warn("Invalid authentication during doneAndPlannedTasks query");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            logger.warn("Error fetching done and planned tasks info", e);
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Returns the total duration (in hours) of all completed tasks
     * for the authenticated user up to the current date and time.
     */
    @GetMapping("/getTotalTaskTime")
    public ResponseEntity<Double> getUserTotalTaskTime() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) authentication.getPrincipal();

            if (currentUser == null) {
                throw new AuthenticationCredentialsNotFoundException("Invalid authentication or authentication missing");
            }

            long id = currentUser.getId();
            double totalTaskTime = taskService.countTotalTaskTimeInMinutes(id, LocalDate.now(), LocalTime.now());
            double hours = Math.round((totalTaskTime / 60) * 10.0) / 10.0;

            logger.info("User ID {} total task time: {} hours", id, hours);
            return ResponseEntity.ok(hours);
        } catch(AuthenticationCredentialsNotFoundException e) {
                logger.warn("Invalid authentication during getTotalTaskTime query");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            logger.warn("Error fetching total task time.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/getLongestTask")
    public ResponseEntity<GetLongestTaskDTO> getUserLongestTask() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) authentication.getPrincipal();

            if (currentUser == null) {
                throw new AuthenticationCredentialsNotFoundException("Invalid authentication or authentication missing");
            }

            long id = currentUser.getId();
            Optional<Task> taskOptional = taskService.getLongestTask(id);
            if (taskOptional.isEmpty()) {
                logger.info("No longest task found for user ID {}", id);
                return ResponseEntity.ok(new GetLongestTaskDTO(0, null, null, null));
            }

            Task longestTask = taskOptional.get();
            double taskLength = taskService.calculateTaskLength(longestTask);

            logger.info("User ID {} longest task: name={}, length={} hours", id, longestTask.getTaskName(), taskLength);
            return ResponseEntity.ok(new GetLongestTaskDTO(
                    taskLength,
                    longestTask.getStartDate(),
                    longestTask.getEndDate(),
                    longestTask.getTaskName()
            ));
        } catch(AuthenticationCredentialsNotFoundException e) {
                logger.warn("Invalid authentication during getLongestTask query");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            logger.warn("Error fetching longest task.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    /**
     * Returns the day on which the authenticated user has the highest number of tasks scheduled
     * and the number of tasks on that day
     *
     * @return DTO containing the peak task day and task count
     */
    @GetMapping("/peakTaskDay")
    public ResponseEntity<PeakTaskDayDTO> getPeakTaskDay() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) authentication.getPrincipal();

            if (currentUser == null) {
                throw new AuthenticationCredentialsNotFoundException("Invalid authentication or authentication missing");
            }

            PeakTaskDayDTO dto = taskService.getUserPeakTaskDay(currentUser.getId());
            logger.info("User ID {} peak task day: {}", currentUser.getId(), dto);
            return ResponseEntity.ok(dto);
        } catch(AuthenticationCredentialsNotFoundException e) {
            logger.warn("Invalid authentication during peakTaskDay query");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            logger.warn("Error fetching longest task.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
