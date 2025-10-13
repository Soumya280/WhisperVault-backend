package com.whispervault.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.whispervault.DTO.MessageDTO.AllPosts;
import com.whispervault.DTO.MessageDTO.EditMessage;
import com.whispervault.DTO.MessageDTO.EditedMessageResponse;
import com.whispervault.DTO.MessageDTO.MyPosts;
import com.whispervault.DTO.MessageDTO.NewMessage;
import com.whispervault.Entity.Message;
import com.whispervault.Entity.User;
import com.whispervault.Repository.MessageRepository;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserService userService;

    public ResponseEntity<?> getAllPosts() {
        try {
            List<AllPosts> posts = messageRepository.findAllWithUser()
                    .stream()
                    .map(post -> new AllPosts(
                            post.getMessageId(),
                            post.getUser().getId(),
                            post.getUser().getUsername(),
                            post.getTitle(),
                            post.getContent(),
                            post.getCreatedAt() != null ? post.getCreatedAt().toString() : null,
                            post.getEdited()))
                    .toList();

            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving posts: " + e.getMessage());
        }
    }

    public ResponseEntity<?> postMessage(NewMessage newMessage) {
        try {
            User user = userService.getCurrentUserDetails();
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            if (newMessage.getContent() == null || newMessage.getContent().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Content cannot be empty");
            }

            Message message = new Message();
            message.setTitle(newMessage.getTitle());
            message.setContent(newMessage.getContent().trim());
            message.setUser(user);

            messageRepository.save(message);
            return ResponseEntity.ok("Post created successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error creating post");
        }
    }

    @Transactional
    public ResponseEntity<?> editMessage(EditMessage editMessage) {
        try {

            User user = userService.getCurrentUserDetails();
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Optional<Message> optionalMessage = messageRepository.findByIdWithUser(editMessage.getMessageId());
            if (optionalMessage.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Message not found");
            }

            Message message = optionalMessage.get();
            if (!message.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authorized to edit this message");
            }

            if (editMessage.getContent() == null || editMessage.getContent().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Content cannot be empty");
            }

            String username = message.getUser().getUsername();

            message.setTitle(editMessage.getTitle());
            message.setContent(editMessage.getContent().trim());
            message.setEdited(true);

            Message updatedMessage = messageRepository.save(message);

            // Map to Response DTO
            EditedMessageResponse dto = new EditedMessageResponse(
                    updatedMessage.getMessageId(),
                    updatedMessage.getTitle(),
                    updatedMessage.getContent(),
                    updatedMessage.getEdited(),
                    username);

            return ResponseEntity.ok(dto);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating message: " + e.getMessage());
        }
    }

    @Transactional
    public ResponseEntity<?> deleteMessage(Integer messageId) {
        try {

            User user = userService.getCurrentUserDetails();
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            Optional<Message> optionalMessage = messageRepository.findById(messageId);
            if (optionalMessage.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Message not found");
            }

            Message message = optionalMessage.get();
            if (!message.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authorized to delete this message");
            }

            messageRepository.delete(message);
            return ResponseEntity.ok("Message deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting message");
        }
    }

    public ResponseEntity<?> getMessage(Integer messageId) {
        try {
            Optional<Message> message = messageRepository.findById(messageId);
            if (message.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Message not found");
            }
            return ResponseEntity.ok(message.get());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving message");
        }
    }

    public ResponseEntity<?> getMyPosts() {
        try {

            User user = userService.getCurrentUserDetails();
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            List<MyPosts> myPosts = messageRepository
                    .findAllByUserIdWithUser(user.getId())
                    .stream()
                    .map(post -> new MyPosts(
                            post.getMessageId(),
                            post.getTitle(),
                            post.getContent(),
                            post.getCreatedAt() != null ? post.getCreatedAt().toString() : null,
                            post.getEdited()))
                    .toList();

            return ResponseEntity.ok(myPosts);
        } catch (Exception e) {
            e.printStackTrace(); // log the actual stacktrace for debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving your posts: " + e.getMessage());
        }
    }

    public ResponseEntity<?> upvote(Integer messageId) {
        try {
            // Upvote logic to be implemented later
            return ResponseEntity.ok("Upvote functionality to be implemented for message ID: " + messageId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing upvote");
        }
    }
}