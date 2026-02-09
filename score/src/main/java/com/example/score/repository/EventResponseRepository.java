package com.example.score.repository;

import com.example.score.entities.EventResponseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventResponseRepository extends JpaRepository<EventResponseEntity, String> {
}
