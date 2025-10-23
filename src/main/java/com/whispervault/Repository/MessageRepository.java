package com.whispervault.Repository;


import com.whispervault.DTO.MessageDTO.AllPosts;
import com.whispervault.DTO.MessageDTO.MyPosts;
import com.whispervault.Entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {

    @Query("""
    SELECT new com.whispervault.DTO.MessageDTO.MyPosts(
        m.messageId,
        m.title,
        m.content,
        m.createdAt,
        m.edited
    )
    FROM Message m
    WHERE m.user.id = :userId
    """)
    List<MyPosts> findAllByUserId(@Param("userId") Integer userId);

    @Query("""
    SELECT new com.whispervault.DTO.MessageDTO.AllPosts(
        m.messageId,
        u.id,
        u.alias,
        m.title,
        m.content,
        CAST(FUNCTION('DATE_FORMAT', m.createdAt, '%Y-%m-%d %H:%i:%s') AS string),
        m.edited
    )
    FROM Message m
    JOIN m.user u
    """)
    List<AllPosts> findAllWithUserDetails();

    Integer countByUserId(Integer userId);

    @Query("SELECT m FROM Message m JOIN FETCH m.user WHERE m.messageId = :id")
    Message findByIdWithUser(@Param("id") Integer id);

}
