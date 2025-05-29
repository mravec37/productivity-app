package com.example.productivity_app.dto;

import com.example.productivity_app.entity.Task;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class ResponseDTO {
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
