package com.example.productivity_app.controller;

import com.example.productivity_app.dto.TaskDTO;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@CrossOrigin(origins = "localhost")
public class DefaultController {

    @PostMapping("/test")
    public void createTask(@RequestBody TaskDTO taskDTO) {
        System.out.println("Triggered!!!!!!!!!!!!!!!!!!");
    }

    @GetMapping("/")
    public String redirectToAuth() {
        return "redirect:/auth/index.html";
    }
}
