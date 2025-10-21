package com.whispervault.Controller;

import org.springframework.web.bind.annotation.RestController;

import com.whispervault.DTO.MessageDTO.EditMessage;
import com.whispervault.DTO.MessageDTO.NewMessage;
import com.whispervault.Service.MessageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class MessageController {

    @Autowired
    private MessageService messageService;

    @GetMapping({ "/home", "/" })
    public ResponseEntity<?> getAllPosts() {
        return messageService.getAllPosts();
    }

    @PostMapping("/createPost")
    public ResponseEntity<?> postMessage(@RequestBody NewMessage newMessage) {
        return messageService.postMessage(newMessage);
    }

    @PutMapping("/editPost")
    public ResponseEntity<?> editMessage(@RequestBody EditMessage message) {
        return messageService.editMessage(message);
    }

    @DeleteMapping("/deleteMessage")
    public ResponseEntity<?> deleteMessage(@RequestParam Integer messageId) {
        return messageService.deleteMessage(messageId);
    }

    @GetMapping("/myMessages")
    public ResponseEntity<?> myMessages() {
        return messageService.getMyPosts();
    }

    @PostMapping("/upvote")
    public ResponseEntity<?> upvote(@RequestParam Integer messageId) {
        return messageService.upvote(messageId);
    }
}