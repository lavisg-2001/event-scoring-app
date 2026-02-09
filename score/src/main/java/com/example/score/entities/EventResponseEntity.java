package com.example.score.entities;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "event_response")
public class EventResponseEntity {

    @Id
    @Column(name = "event_id")
    private String eventId;

    @Column(name = "response_json", columnDefinition = "TEXT")
    private String responseJson;
}
