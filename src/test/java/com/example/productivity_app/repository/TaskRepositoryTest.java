package com.example.productivity_app.repository;

import com.example.productivity_app.entity.Task;
import com.example.productivity_app.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveTaskToDatabase() {

        User user = new User();
        user.setUsername("springtester");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setEnabled(true);
        userRepository.save(user);

        Task task = Task.builder()
                .taskName("Ucit sa Spring Boot")
                .startDate(LocalDate.of(2024, 5, 1))
                .endDate(LocalDate.of(2024, 5, 1))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(12, 0))
                .taskDescription("Spring Boot testovanie")
                .taskColor("#FF5733")
                .user(user)
                .build();

        Task savedTask = taskRepository.save(task);

        assertThat(savedTask.getId()).isNotNull();
        assertThat(savedTask.getTaskName()).isEqualTo("Ucit sa Spring Boot");
        assertThat(savedTask.getUser().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void shouldNotCreateOverlappingTaskForSameUser() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setEnabled(true);
        userRepository.save(user);

        int firstInsert = taskRepository.createTaskIfNoOverlap(
                "Prva uloha",
                LocalTime.of(10, 0),
                LocalTime.of(12, 0),
                LocalDate.of(2025, 5, 25),
                LocalDate.of(2025, 5, 25),
                "Toto je prva uloha",
                "#FF0000",
                "testuser"
        );

        int secondInsert = taskRepository.createTaskIfNoOverlap(
                "Druha uloha",
                LocalTime.of(11, 0),
                LocalTime.of(13, 0),
                LocalDate.of(2025, 5, 25),
                LocalDate.of(2025, 5, 25),
                "Tato uloha overlappuje prvu",
                "#00FF00",
                "testuser"
        );

        assertThat(firstInsert).isEqualTo(1);
        assertThat(secondInsert).isEqualTo(0);

        List<Task> tasks = taskRepository.findAll();
        assertThat(tasks).hasSize(1);
        assertThat(tasks.get(0).getTaskName()).isEqualTo("Prva uloha");
    }

}