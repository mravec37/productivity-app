package com.example.productivity_app.dto.user_authentication;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {

    private String token;
    private long expiresIn;

    public LoginResponse(String token) {
        this.token = token;
        //this.expiresIn = expiresIn;
    }
}
