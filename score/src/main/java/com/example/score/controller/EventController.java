package com.example.score.controller;

import com.example.score.beans.Event;
import com.example.score.beans.EventResponse;
import com.example.score.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/events")
@CrossOrigin(origins = "http://localhost:4200")
public class EventController {
    @Autowired
    private EventService eventService;

    @PostMapping("/score")
    public ResponseEntity<EventResponse> getEventResponse(@RequestBody Event event) {
        return eventService.getEventResponse(event);
    }

    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("Controller is working!");
    }
}