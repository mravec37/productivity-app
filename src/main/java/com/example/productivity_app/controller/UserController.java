package com.example.productivity_app.controller;


import com.example.productivity_app.dto.GetUserDoneAndPlannedTaskInfoDTO;
import com.example.productivity_app.entity.User;
import com.example.productivity_app.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@RequestMapping("/users")
@Controller
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    @GetMapping("/me")
    public ResponseEntity<User> authenticatedUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) authentication.getPrincipal();
            if (currentUser == null) {
                throw new AuthenticationCredentialsNotFoundException("Invalid token or authentication missing");
            }
            return ResponseEntity.ok(currentUser);

        } catch (AuthenticationCredentialsNotFoundException e) {
            e.printStackTrace();
            logger.warn("Invalid authentication during /me query");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/getUsername")
    public ResponseEntity<String> getUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) authentication.getPrincipal();
            if (currentUser == null) {
                throw new AuthenticationCredentialsNotFoundException("Invalid token or authentication missing");
            }
            return ResponseEntity.ok(currentUser.getUsername());

        } catch (AuthenticationCredentialsNotFoundException e) {
            e.printStackTrace();
            logger.warn("Invalid authentication during getUsername query");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/")
    public ResponseEntity<List<User>> allUsers() {
        List<User> users = userService.allUsers();
        return ResponseEntity.ok(users);
    }

}
