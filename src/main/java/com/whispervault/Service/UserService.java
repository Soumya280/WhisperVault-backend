package com.whispervault.Service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.whispervault.DTO.UserDTO.Signup;
import com.whispervault.Entity.User;
import com.whispervault.Repository.MessageRepository;
import com.whispervault.Repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private record UserProfile(
            Integer Id,
            String email,
            String username,
            String alias,
            Integer messages) {
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    // returns a User object to be used by other methods
    public User getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }

        return getUserByUsername(authentication.getName());
    }



    // return read only response entity.
    public ResponseEntity<?> getCurrentUser() {
        try {
            User user = getCurrentUserDetails();
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            UserProfile profile = new UserProfile(
                    user.getId(),
                    user.getEmail(),
                    user.getUsername(),
                    user.getAlias(),
                    messageRepository.countByUserId(user.getId()));

            return ResponseEntity.ok(profile);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve user"));
        }
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
            user.setPassword(passwordEncoder.encode(signup.getPassword()));

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

            User currentUser = getCurrentUserDetails();
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
            currentUser.setPassword(update.getPassword());

            User updatedUser = userRepository.save(currentUser);
            return ResponseEntity.ok(updatedUser);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Update failed"));
        }
    }

    public ResponseEntity<?> getUserById(Integer userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to retrieve user"));
        }
    }
}