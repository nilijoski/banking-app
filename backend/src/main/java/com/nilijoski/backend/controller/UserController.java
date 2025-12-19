package com.nilijoski.backend.controller;

import com.nilijoski.backend.model.User;
import com.nilijoski.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");
            String firstName = request.get("firstName");
            String lastName = request.get("lastName");

            User user = userService.register(username, password, firstName, lastName);
            return new ResponseEntity<>(user, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
    
    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");

            User user = userService.login(username, password);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }
    
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<User> getAccountByAccountNumber(@PathVariable String accountNumber) {
        try {
            User user = userService.getUserByAccountNumber(accountNumber);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @GetMapping("/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        User user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/{userId}/saved-recipients")
    public ResponseEntity<User> addSavedRecipient(@PathVariable String userId, @RequestBody Map<String, String> request) {
        String recipientIban = request.get("recipientIban");
        User user = userService.addSavedRecipient(userId, recipientIban);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/{userId}/saved-recipients")
    public ResponseEntity<List<User>> getSavedRecipients(@PathVariable String userId) {
        List<User> recipients = userService.getSavedRecipients(userId);
        return ResponseEntity.ok(recipients);
    }

    @DeleteMapping("/{userId}/saved-recipients/{recipientIban}")
    public ResponseEntity<User> removeSavedRecipient(@PathVariable String userId, @PathVariable String recipientIban) {
        User user = userService.removeSavedRecipient(userId, recipientIban);
        return ResponseEntity.ok(user);
    }
}
