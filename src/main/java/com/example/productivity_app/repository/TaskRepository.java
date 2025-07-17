package com.example.productivity_app.repository;

import com.example.productivity_app.entity.Task;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByStartDate(LocalDate startDate);

    @Query("SELECT t FROM Task t WHERE " +
            "(t.startDate BETWEEN :startDate AND :endDate) " +
            "OR ((t.startDate < :startDate) AND (t.endDate >= :startDate))")
    List<Task> findTasksByStartOrEndTimeInRange(@Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);

    @Query("SELECT t FROM Task t JOIN FETCH t.user u WHERE " +
            "((t.startDate BETWEEN :startDate AND :endDate) " +
            "OR (t.startDate < :startDate AND t.endDate >= :startDate)) " +
            "AND u.username = :username")
    List<Task> findTasksByUsernameAndStartOrEndTimeInRange(
            @Param("username") String username,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);


    @Modifying
    @Transactional
    @Query(value = """
    INSERT INTO tasks (task_name, start_time, end_time, start_date, end_date, task_description, task_color, user_id)
    SELECT :taskName, :startTime, :endTime, :startDate, :endDate, :taskDescription, :taskColor, u.id
    FROM users u
    WHERE u.username = :username
    AND NOT EXISTS (
        SELECT 1 FROM tasks t
        WHERE ( 
            (:endDate = t.start_date AND :endTime > t.start_time) OR :endDate > t.start_date
        )
        AND (
            (t.end_date = :startDate AND :startTime < t.end_time) OR :startDate < t.end_date
        )
        AND t.user_id = u.id
    )
    """,
            nativeQuery = true)
    int createTaskIfNoOverlap(@Param("taskName") String taskName,
                              @Param("startTime") LocalTime startTime,
                              @Param("endTime") LocalTime endTime,
                              @Param("startDate") LocalDate startDate,
                              @Param("endDate") LocalDate endDate,
                              @Param("taskDescription") String taskDescription,
                              @Param("taskColor") String taskColor,
                              @Param("username") String username);

    @Query("SELECT t FROM Task t JOIN t.user u " +
            "WHERE t.startDate = :startDate " +
            "AND t.startTime = :startTime " +
            "AND u.username = :userName")
    Optional<Task> findByUserStartDateAndStartTime(
            @Param("startDate") LocalDate startDate,
            @Param("startTime") LocalTime startTime,
            @Param("userName") String userName);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.user.id = :userId AND (t.endDate < :nowDate OR (t.endDate = :nowDate AND t.endTime <= :nowTime))")
    int countDoneTasks(@Param("userId") Long userId, @Param("nowDate") LocalDate nowDate, @Param("nowTime") LocalTime nowTime);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.user.id = :userId AND (t.endDate > :nowDate OR (t.endDate = :nowDate AND t.endTime > :nowTime))")
    int countPlannedTasks(@Param("userId") Long userId, @Param("nowDate") LocalDate nowDate, @Param("nowTime") LocalTime nowTime);



    @Modifying
    @Transactional
    @Query(value = "UPDATE tasks " +
            "SET task_name = :taskName, start_time = :startTime, end_time = :endTime, start_date = :startDate, end_date = :endDate, task_description = :taskDescription, task_color = :taskColor " +
            "WHERE id = :id " +
            "AND NOT EXISTS (" +
            "    SELECT 1 " +
            "    FROM (SELECT * FROM tasks) AS temp_tasks " +
            "    WHERE temp_tasks.id != :id " +
            "    AND ((:endDate = temp_tasks.start_date AND :endTime > temp_tasks.start_time) OR :endDate > temp_tasks.start_date) " +
            "    AND ((temp_tasks.end_date = :startDate AND :startTime < temp_tasks.end_time) OR :startDate < temp_tasks.end_date)" +
            ")",
            nativeQuery = true)
    int updateTaskIfNoOverlap(@Param("id") Long id,
                              @Param("taskName") String taskName,
                              @Param("startTime") LocalTime startTime,
                              @Param("endTime") LocalTime endTime,
                              @Param("startDate") LocalDate startDate,
                              @Param("endDate") LocalDate endDate,
                              @Param("taskDescription") String taskDescription,
                              @Param("taskColor") String taskColor);

    @Query(value = """
    SELECT SUM(TIMESTAMPDIFF(MINUTE,
        CONCAT(t.start_date, ' ', t.start_time), 
        CONCAT(t.end_date, ' ', t.end_time)))
    FROM tasks t
    WHERE t.user_id = :id
      AND (
        t.end_date < :nowDate OR
        (t.end_date = :nowDate AND t.end_time < :nowTime)
      )
      AND CONCAT(t.start_date, ' ', t.start_time) <= CONCAT(t.end_date, ' ', t.end_time)
""", nativeQuery = true)
    Integer countTotalTaskTimeInMinutes(@Param("id") long id,
                                    @Param("nowDate") LocalDate nowDate,
                                    @Param("nowTime") LocalTime nowTime);

    @Query(value = "SELECT * FROM tasks " +
            "WHERE user_id = :userId " +
            "ORDER BY TIMESTAMPDIFF(MINUTE, CONCAT(start_date, ' ', start_time), CONCAT(end_date, ' ', end_time)) DESC " +
            "LIMIT 1",
            nativeQuery = true)
    Optional<Task> getLongestTask(@Param("userId") long userId);

    @Query(value = """
                    SELECT active.day AS date, COUNT(*) AS task_count
                           FROM (
                               SELECT d.day, t.id
                               FROM tasks t
                               JOIN (
                                   SELECT DISTINCT start_date AS day FROM tasks
                                   UNION
                                   SELECT DISTINCT end_date AS day FROM tasks
                               ) d ON t.start_date <= d.day AND t.end_date >= d.day
                               WHERE t.user_id = :userId
                           ) AS active
                           GROUP BY active.day
                           ORDER BY task_count DESC
                           LIMIT 1
                           
            """, nativeQuery = true)
    Map<String, Object> findPeakTaskDayForUser(@Param("userId") Long userId);

}
