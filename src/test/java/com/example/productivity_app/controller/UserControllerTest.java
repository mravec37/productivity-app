package com.example.productivity_app.controller;

import com.example.productivity_app.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserControllerTest.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnUsernameWhenAuthenticated() throws Exception {
        User mockUser = new User("mockUser", "mock@example.com", "password");
        mockUser.setId(1L);

        Authentication auth = new UsernamePasswordAuthenticationToken(mockUser, null, new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(auth);

        mockMvc.perform(get("/getUsername"))
                .andExpect(status().isOk())
                .andExpect(content().string("mockUser"));
    }
}
