package com.example.productivity_app.controller;

import com.example.productivity_app.dto.GetTasksResponseDTO;
import com.example.productivity_app.entity.Task;
import com.example.productivity_app.entity.User;
import com.example.productivity_app.repository.TaskRepository;
import com.example.productivity_app.service.JwtService;
import com.example.productivity_app.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private TaskRepository taskRepository;

    @Test
    void shouldReturnTasksForAuthenticatedUserWithinDateRange() throws Exception {
        String username = "testuser";
        LocalDate startDate = LocalDate.of(2025, 6, 1);
        LocalDate endDate = LocalDate.of(2025, 6, 5);

        Task task1 = Task.builder()
                .id(1L)
                .taskName("Task Jedna")
                .taskDescription("Desc 1")
                .taskColor("red")
                .startDate(startDate)
                .endDate(endDate)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .user(new User())
                .build();

        Task task2 = Task.builder()
                .id(2L)
                .taskName("Task Dva")
                .taskDescription("Desc 2")
                .taskColor("blue")
                .startDate(startDate)
                .endDate(endDate)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .user(new User())
                .build();

        List<Task> tasks = List.of(task1, task2);

        when(taskService.getTasksByDateForUser(eq(username), eq(startDate), eq(endDate))).thenReturn(tasks);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(username, null, List.of())
        );

        mockMvc.perform(get("/task/getTasks")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Success"))
                .andExpect(jsonPath("$.tasksList").isArray())
                .andExpect(jsonPath("$.tasksList.length()").value(2))
                .andExpect(jsonPath("$.tasksList[0].taskName").value("Task Jedna"))
                .andExpect(jsonPath("$.tasksList[1].taskName").value("Task Dva"));
    }
}
