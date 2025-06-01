package com.example.productivity_app.service;

import com.example.productivity_app.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void shouldReturnTotalTaskTimeWhenRepositoryReturnsValue() {
        long userId = 1L;
        LocalDate date = LocalDate.of(2025, 6, 1);
        LocalTime time = LocalTime.of(12, 0);
        when(taskRepository.countTotalTaskTimeInMinutes(userId, date, time)).thenReturn(120);

        int result = taskService.countTotalTaskTimeInMinutes(userId, date, time);

        assertThat(result).isEqualTo(120);
        verify(taskRepository).countTotalTaskTimeInMinutes(userId, date, time);
    }

    @Test
    void shouldReturnZeroWhenRepositoryReturnsNull() {
        long userId = 2L;
        LocalDate date = LocalDate.of(2025, 6, 1);
        LocalTime time = LocalTime.of(15, 0);
        when(taskRepository.countTotalTaskTimeInMinutes(userId, date, time)).thenReturn(null);

        int result = taskService.countTotalTaskTimeInMinutes(userId, date, time);

        assertThat(result).isZero();
        verify(taskRepository).countTotalTaskTimeInMinutes(userId, date, time);
    }
}
