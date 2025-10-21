package com.whispervault.Controller;

import org.springframework.web.bind.annotation.RestController;

import com.whispervault.DTO.UserDTO.Signup;
import com.whispervault.Service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Signup signup) {
        return userService.signup(signup);
    }

    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestBody Signup update) {
        return userService.update(update);
    }

    @GetMapping("/getuser")
    public ResponseEntity<?> getUser() {
        return userService.getCurrentUser();
    }

}