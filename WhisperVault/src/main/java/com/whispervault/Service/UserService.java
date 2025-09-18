package com.whispervault.Service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.whispervault.DTO.UserDTO.Signup;
import com.whispervault.DTO.UserDTO.UserProfile;
import com.whispervault.Entity.User;
import com.whispervault.Repository.MessageRepository;
import com.whispervault.Repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return userRepository.findByUsername(authentication.getName());
        }
        return null;
    }

    @Transactional
    public ResponseEntity<?> signup(Signup signup) {
        try {
            if (userRepository.existsByEmail(signup.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Email already exists"));
            }

            if (userRepository.existsByUsername(signup.getUsername())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Username already exists"));
            }

            User user = new User();
            user.setEmail(signup.getEmail().trim());
            user.setUsername(signup.getUsername().trim());
            user.setAlias(signup.getAlias() != null ? signup.getAlias().trim() : signup.getUsername().trim());
            user.setPassword(signup.getPassword()); // No encoding - stored as plain text

            User newUser = userRepository.save(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(newUser);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Registration failed"));
        }
    }

    @Transactional
    public ResponseEntity<?> update(Signup update) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            User currentUser = userRepository.findByUsername(auth.getName());
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            // Check email availability (if changed)
            if (!currentUser.getEmail().equals(update.getEmail()) &&
                    userRepository.existsByEmail(update.getEmail())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Email already taken"));
            }

            // Check username availability (if changed)
            if (!currentUser.getUsername().equals(update.getUsername()) &&
                    userRepository.existsByUsername(update.getUsername())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Username already taken"));
            }

            // Update user fields
            currentUser.setEmail(update.getEmail().trim());
            currentUser.setUsername(update.getUsername().trim());
            currentUser.setAlias(update.getAlias() != null ? update.getAlias().trim() : update.getUsername().trim());
            currentUser.setPassword(update.getPassword()); // No encoding - stored as plain text

            User updatedUser = userRepository.save(currentUser);
            return ResponseEntity.ok(updatedUser);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Update failed"));
        }
    }

    public ResponseEntity<?> getUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            User user = userRepository.findByUsername(auth.getName());
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            UserProfile profile = new UserProfile();

            profile.setId(user.getId());
            profile.setEmail(user.getEmail());
            profile.setUsername(user.getUsername());
            profile.setAlias(user.getAlias());
            profile.setMessages(messageRepository.countByUserId(user.getId()));

            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve user"));
        }
    }

    public ResponseEntity<?> getUserById(Integer userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve user"));
        }
    }
}