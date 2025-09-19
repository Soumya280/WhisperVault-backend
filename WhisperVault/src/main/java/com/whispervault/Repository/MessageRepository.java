package com.whispervault.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.whispervault.Entity.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {

    @Query("SELECT m FROM Message m JOIN FETCH m.user WHERE m.user.id = :userId ORDER BY m.createdAt DESC")
    List<Message> findAllByUserIdWithUser(@Param("userId") Integer userId);

    @Query("SELECT m FROM Message m JOIN FETCH m.user ORDER BY m.createdAt DESC")
    List<Message> findAllWithUser();

    Integer countByUserId(Integer userId);

    @Query("SELECT m FROM Message m JOIN FETCH m.user WHERE m.messageId = :id")

    Optional<Message> findByIdWithUser(@Param("id") Integer id);

}
