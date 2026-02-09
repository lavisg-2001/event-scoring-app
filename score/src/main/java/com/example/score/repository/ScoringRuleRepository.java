package com.example.score.repository;

import com.example.score.entities.ScoringRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScoringRuleRepository extends JpaRepository<ScoringRule, Long> {
}
