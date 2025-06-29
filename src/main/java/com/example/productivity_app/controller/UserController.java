package com.example.productivity_app.controller;


import com.example.productivity_app.dto.GetUserDoneAndPlannedTaskInfoDTO;
import com.example.productivity_app.entity.User;
import com.example.productivity_app.service.UserService;
import org.springframework.http.ResponseEntity;
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


    @GetMapping("/me")
    public ResponseEntity<User> authenticatedUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) authentication.getPrincipal();
            if (currentUser == null) {
                throw new RuntimeException("Invalid token");
            }
            return ResponseEntity.ok(currentUser);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/getUsername")
    public ResponseEntity<String> getUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) authentication.getPrincipal();
            if (currentUser == null) {
                throw new RuntimeException("Invalid token");
            }
            return ResponseEntity.ok(currentUser.getUsername());

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/")
    public ResponseEntity<List<User>> allUsers() {
        List<User> users = userService.allUsers();
        return ResponseEntity.ok(users);
    }

}
