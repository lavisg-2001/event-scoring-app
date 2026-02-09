package com.example.score.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "scoring_rule")
public class ScoringRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_json", columnDefinition = "TEXT")
    private String ruleJson;
}