package com.example.score.service;

import com.example.score.beans.Event;
import com.example.score.beans.EventResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface EventService {
    ResponseEntity<EventResponse> getEventResponse(Event event);
}
