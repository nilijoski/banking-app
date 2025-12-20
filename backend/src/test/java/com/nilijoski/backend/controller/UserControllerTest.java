package com.nilijoski.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nilijoski.backend.model.User;
import com.nilijoski.backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User mockUser() {
        User user = new User();
        user.setId("u1");
        user.setUsername("johndoe");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setAccountNumber("12345");
        return user;
    }

    @Test
    void register_success() throws Exception {
        when(userService.register(any(), any(), any(), any()))
                .thenReturn(mockUser());

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of(
                                        "username", "johndoe",
                                        "password", "pw",
                                        "firstName", "John",
                                        "lastName", "Doe"
                                )
                        )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("johndoe"));
    }

    @Test
    void register_badRequest() throws Exception {
        when(userService.register(any(), any(), any(), any()))
                .thenThrow(new RuntimeException());

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_success() throws Exception {
        when(userService.login("johndoe", "pw"))
                .thenReturn(mockUser());

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("username", "johndoe", "password", "pw")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("johndoe"));
    }

    @Test
    void login_unauthorized() throws Exception {
        when(userService.login(any(), any()))
                .thenThrow(new RuntimeException());

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAllUsers_success() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(mockUser()));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getAccountByAccountNumber_success() throws Exception {
        when(userService.getUserByAccountNumber("12345"))
                .thenReturn(mockUser());

        mockMvc.perform(get("/api/users/number/12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("12345"));
    }

    @Test
    void getAccountByAccountNumber_notFound() throws Exception {
        when(userService.getUserByAccountNumber("12345"))
                .thenThrow(new RuntimeException());

        mockMvc.perform(get("/api/users/number/12345"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserByUsername_success() throws Exception {
        when(userService.getUserByUsername("johndoe"))
                .thenReturn(mockUser());

        mockMvc.perform(get("/api/users/johndoe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("johndoe"));
    }

    @Test
    void addSavedRecipient_success() throws Exception {
        when(userService.addSavedRecipient("u1", "IBAN1"))
                .thenReturn(mockUser());

        mockMvc.perform(post("/api/users/u1/saved-recipients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("recipientIban", "IBAN1")
                        )))
                .andExpect(status().isOk());
    }

    @Test
    void getSavedRecipients_success() throws Exception {
        when(userService.getSavedRecipients("u1"))
                .thenReturn(List.of(mockUser()));

        mockMvc.perform(get("/api/users/u1/saved-recipients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void removeSavedRecipient_success() throws Exception {
        when(userService.removeSavedRecipient("u1", "IBAN1"))
                .thenReturn(mockUser());

        mockMvc.perform(delete("/api/users/u1/saved-recipients/IBAN1"))
                .andExpect(status().isOk());
    }
}
