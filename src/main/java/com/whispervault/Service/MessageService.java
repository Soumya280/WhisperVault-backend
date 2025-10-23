package com.whispervault.Service;

import java.util.List;
import java.util.Optional;

import com.whispervault.DTO.MessageDTO.AllPosts;
import com.whispervault.DTO.MessageDTO.EditMessage;
import com.whispervault.DTO.MessageDTO.MyPosts;
import com.whispervault.DTO.MessageDTO.NewMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.whispervault.Entity.Message;
import com.whispervault.Entity.User;
import com.whispervault.Repository.MessageRepository;
import org.springframework.web.server.ResponseStatusException;

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

    public ResponseEntity<?> getAllPosts() {
        try {
            List<AllPosts> posts = messageRepository.findAllWithUserDetails()
                    .stream()
                    .map(post -> new AllPosts(
                            post.messageId(),
                            post.userId(),
                            post.alias(),
                            post.title(),
                            post.content(),
                            post.createdAt(),
                            post.edited()))
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

            Message realMessage = messageRepository.findByIdWithUser(editMessage.messageId());
            if (realMessage == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Message not found");
            }

            if (!realMessage.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authorized to edit this message");
            }

            if (editMessage.content() == null || editMessage.content().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Content cannot be empty");
            }

            String username = realMessage.getUser().getUsername();

            realMessage.setTitle(editMessage.title());
            realMessage.setContent(editMessage.content().trim());
            realMessage.setEdited(true);

            Message updatedMessage = messageRepository.save(realMessage);

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

            Message message = messageRepository.findById(messageId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found"));

            if (!message.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not authorized to delete this message");
            }

            messageRepository.delete(message);
            return ResponseEntity.ok("Message deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting message");
        }
    }

    public ResponseEntity<?> getMyPosts() {
        try {

            User user = userService.getCurrentUserDetails();
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            List<MyPosts> myPosts = messageRepository
                    .findAllByUserId(user.getId())
                    .stream()
                    .map(post -> new MyPosts(
                            post.messageId(),
                            post.title(),
                            post.content(),
                            post.createdAt() != null ? post.createdAt() : null,
                            post.edited()))
                    .toList();

            return ResponseEntity.ok(myPosts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving your posts: " + e.getMessage());
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


    public ResponseEntity<?> upvote(Integer messageId) {
        try {
            // Upvote logic to be implemented later

            return ResponseEntity.ok("Upvote functionality to be implemented for message ID: " + messageId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing upvote");
        }
    }
}