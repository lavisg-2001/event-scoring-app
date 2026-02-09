package com.example.score.service;

import com.example.score.beans.Event;
import com.example.score.beans.EventResponse;
import com.example.score.entities.EventResponseEntity;
import com.example.score.entities.ScoringRule;
import com.example.score.repository.EventResponseRepository;
import com.example.score.repository.ScoringRuleRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class EventServiceImpl implements EventService {
    @Autowired
    private ScoringRuleRepository ruleRepo;

    @Autowired
    private EventResponseRepository responseRepo;

    private List<ScoringRule> rules;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void loadRules() {
        rules = ruleRepo.findAll();
    }

    public ResponseEntity<EventResponse> getEventResponse(Event event) {
        // Input validation: Check for invalid request
        if (event == null || event.getEventId() == null || event.getEventId().isEmpty()) {
            EventResponse errorResponse = EventResponse.builder()
                    .eventId(event != null ? event.getEventId() : "unknown")
                    .score(0.0)
                    .confidence(0.0)
                    .flags(List.of("ERROR"))
                    .reasons(List.of("Invalid request: eventId is required and cannot be null or empty"))
                    .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // 1. Check for existing response (idempotency)
        Optional<EventResponseEntity> existing = responseRepo.findById(event.getEventId());
        if (existing.isPresent()) {
            try {
                EventResponse response = parseJsonToResponse(existing.get().getResponseJson());
                return ResponseEntity.ok(response);
            } catch (JsonProcessingException e) {
                EventResponse errorResponse = EventResponse.builder()
                        .eventId(event.getEventId())
                        .score(0.0)
                        .confidence(0.0)
                        .flags(List.of("ERROR"))
                        .reasons(List.of("Data error: Invalid JSON in stored response"))
                        .build();
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
        }

        // 2. Compute new response
        EventResponse response;
        try {
            response = computeScore(event);
        } catch (JsonProcessingException e) {
            EventResponse errorResponse = EventResponse.builder()
                    .eventId(event.getEventId())
                    .score(0.0)
                    .confidence(0.0)
                    .flags(List.of("ERROR"))
                    .reasons(List.of("Processing error: Failed to evaluate rules due to invalid JSON in rules data"))
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        // 3. Store it
        EventResponseEntity entity = new EventResponseEntity();
        entity.setEventId(event.getEventId());
        try {
            entity.setResponseJson(convertResponseToJson(response));
        } catch (JsonProcessingException e) {
            EventResponse errorResponse = EventResponse.builder()
                    .eventId(event.getEventId())
                    .score(0.0)
                    .confidence(0.0)
                    .flags(List.of("ERROR"))
                    .reasons(List.of("Serialization error: Failed to store response due to JSON conversion issue"))
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
        responseRepo.save(entity);

        return ResponseEntity.ok(response);
    }

    private EventResponse computeScore(Event event) throws JsonProcessingException {
        double totalScore = 0.0;
        double totalConfidence = 1.0;
        List<String> flags = new ArrayList<>();
        List<String> reasons = new ArrayList<>();
        boolean anyRuleMatched = false;

        for (ScoringRule rule : rules) {
            if (matchesRule(rule, event)) {
                anyRuleMatched = true;
                JsonNode ruleNode = objectMapper.readTree(rule.getRuleJson());
                totalScore += ruleNode.get("scoreImpact").asDouble();
                if (ruleNode.has("confidenceImpact")) {
                    totalConfidence += ruleNode.get("confidenceImpact").asDouble();
                }
                if (ruleNode.has("flags")) {
                    ruleNode.get("flags").forEach(flag -> flags.add(flag.asText()));
                }
                if (ruleNode.has("reasons")) {
                    ruleNode.get("reasons").forEach(reason -> reasons.add(reason.asText()));
                }
            }
        }

        // Edge case: No rules matched
        if (!anyRuleMatched) {
            totalScore = 0.0;
            totalConfidence = 1.0;
            // flags and reasons remain empty
        }

        // Cap score (0-100) and confidence (0-1)
        totalScore = Math.max(0, Math.min(100, totalScore));
        totalConfidence = Math.max(0, Math.min(1, totalConfidence));

        EventResponse response = new EventResponse();
        response.setEventId(event.getEventId());
        response.setScore(totalScore);
        response.setConfidence(totalConfidence);
        response.setFlags(flags);
        response.setReasons(reasons);
        return response;
    }

    private boolean matchesRule(ScoringRule rule, Event event) throws JsonProcessingException {
        JsonNode ruleNode = objectMapper.readTree(rule.getRuleJson());

        // Check eventType match
        String ruleEventType = ruleNode.get("eventType").asText();
        if (!ruleEventType.equals(event.getEventType().name())) {
            return false;
        }

        // Evaluate conditions
        JsonNode conditions = ruleNode.get("conditions");
        if (conditions != null && conditions.isArray()) {
            for (JsonNode condition : conditions) {
                String field = condition.get("field").asText();
                String operator = condition.get("operator").asText();
                JsonNode valueNode = condition.get("value");

                Object payloadValue = event.getPayload().get(field);
                if (payloadValue == null) {
                    return false;
                }
                if (!evaluateCondition(payloadValue, operator, valueNode)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean evaluateCondition(Object payloadValue, String operator, JsonNode valueNode) {
        if (payloadValue instanceof Number && valueNode.isNumber()) {
            double pv = ((Number) payloadValue).doubleValue();
            double rv = valueNode.asDouble();
            switch (operator) {
                case "==": return pv == rv;
                case ">": return pv > rv;
                case "<": return pv < rv;
                case ">=": return pv >= rv;
                case "<=": return pv <= rv;
                default: return false;
            }
        } else if (payloadValue instanceof String && valueNode.isTextual()) {
            String pv = (String) payloadValue;
            String rv = valueNode.asText();
            return "==".equals(operator) && pv.equals(rv);
        }
        return false;
    }

    private EventResponse parseJsonToResponse(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, EventResponse.class);
    }

    private String convertResponseToJson(EventResponse response) throws JsonProcessingException {
        return objectMapper.writeValueAsString(response);
    }
}