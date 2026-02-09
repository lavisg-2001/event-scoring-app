package com.example.score.beans;

import com.example.score.enums.EventType;
import lombok.Data;

import java.util.Map;

@Data
public class Event {
    String eventId;
    EventType eventType;
    private Map<String, Object> payload;
}
