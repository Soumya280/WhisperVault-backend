package com.whispervault.Repository;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.whispervault.Entity.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {

    List<Message> findAllByUserId(Integer attribute, Sort sort);

    Integer countByUserId(Integer userId);

}
