package com.whispervault.Service;

import java.util.List;
import java.util.Optional;

import com.whispervault.DTO.MessageDTO.EditMessage;
import com.whispervault.DTO.MessageDTO.NewMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.whispervault.Entity.Message;
import com.whispervault.Entity.User;
import com.whispervault.Repository.MessageRepository;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserService userService;

    private record EditedMessageResponse(
            Integer messageId,
            String title,
            String content,
            Boolean edited,
            String username
    ) {}

    private record AllPosts(
            Integer messageId,
            Integer userId,
            String alias,
            String title,
            String content,
            String createdAt,
            Boolean edited
    ) {}

    private record MyPosts(
            Integer messageId,
            String title,
            String content,
            String createdAt,
            Boolean edited
    ) {}

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

            if (newMessage.content() == null || newMessage.content().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Content cannot be empty");
            }

            Message message = new Message();
            message.setTitle(newMessage.title());
            message.setContent(newMessage.title().trim());
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

            Optional<Message> optionalMessage = messageRepository.findByIdWithUser(editMessage.messageId());
            if (optionalMessage.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Message not found");
            }

            Message message = optionalMessage.get();
            if (!message.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authorized to edit this message");
            }

            if (editMessage.content() == null || editMessage.content().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Content cannot be empty");
            }

            String username = message.getUser().getUsername();

            message.setTitle(editMessage.title());
            message.setContent(editMessage.content().trim());
            message.setEdited(true);

            Message updatedMessage = messageRepository.save(message);

            EditedMessageResponse response = new EditedMessageResponse(
                    updatedMessage.getMessageId(),
                    updatedMessage.getTitle(),
                    updatedMessage.getContent(),
                    updatedMessage.getEdited(),
                    username);

            return ResponseEntity.ok(response);

        } catch (Exception e) {

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