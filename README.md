# Event Scoring App

A full-stack application for event scoring with rules-based evaluation.

## Setup

### Backend (Spring Boot)
- Navigate to `backend/`.
- Run `mvn clean install`.
- Configure MySQL in `src/main/resources/application.properties`.
- Run `mvn spring-boot:run` (starts on port 8081).

### Frontend (Angular)
- Navigate to `frontend/`.
- Run `npm install`.
- Run `ng serve` (starts on port 4200).

## API Endpoints
- POST /events/score: Score an event.

## Testing
Use the provided payloads in the README or Postman.

3. Run `mvn clean install` to build.
4. Run `mvn spring-boot:run` to start the server (runs on port 8081).

# Test Payloads for Event Scoring API

Use these payloads to test the API via the frontend (Angular app at `http://localhost:4200`) or Postman (POST to `http://localhost:8081/events/score` with `Content-Type: application/json` in the headers).

The API evaluates events based on rules stored in the database. Responses include score, confidence, flags, and reasons. The system ensures idempotent results (same input always produces the same output).

## High-Value ORDER with New Customer (Matches rules 1 and 2)
**Payload:**
```json
{
  "eventId": "EVT-101",
  "eventType": "ORDER",
  "payload": {
    "amount": 250000,
    "customerType": "NEW"
  }
}
```

### Database Setup
Run the following SQL queries in your MySQL console to set up the database and insert rules:

```sql
CREATE DATABASE IF NOT EXISTS event_scoring;
USE event_scoring;

CREATE TABLE scoring_rule (
 id BIGINT AUTO_INCREMENT PRIMARY KEY,
 rule_json TEXT NOT NULL
);

CREATE TABLE event_response (
 event_id VARCHAR(255) PRIMARY KEY,
 response_json TEXT NOT NULL
);

INSERT INTO scoring_rule (rule_json) VALUES ('{"ruleId":"high_value_order","eventType":"ORDER","conditions":[{"field":"amount","operator":">","value":100000},{"field":"customerType","operator":"==","value":"NEW"}],"scoreImpact":20.0,"confidenceImpact":0.1,"flags":["HIGH_VALUE"],"reasons":["High value order detected","New customer risk applied"]}');

INSERT INTO scoring_rule (rule_json)
SELECT '{
"ruleId": "base_order_score",
"eventType": "ORDER",
"conditions": [],
"scoreImpact": 50.0,
"confidenceImpact": 0.75,
"flags": ["ORDER_PROCESSED"],
"reasons": ["Base score for order event"]
}'
WHERE NOT EXISTS (
 SELECT 1 FROM scoring_rule 
 WHERE rule_json = '{
"ruleId": "base_order_score",
"eventType": "ORDER",
"conditions": [],
"scoreImpact": 50.0,
"confidenceImpact": 0.75,
"flags": ["ORDER_PROCESSED"],
"reasons": ["Base score for order event"]
}'
);

INSERT INTO scoring_rule (rule_json)
SELECT '{
"ruleId": "low_value_order_risk",
"eventType": "ORDER",
"conditions": [
 {"field": "amount", "operator": "<", "value": 50000}
],
"scoreImpact": -10.0,
"flags": ["LOW_VALUE"],
"reasons": ["Low value order detected, potential risk"]
}'
WHERE NOT EXISTS (
 SELECT 1 FROM scoring_rule 
 WHERE rule_json = '{
"ruleId": "low_value_order_risk",
"eventType": "ORDER",
"conditions": [
 {"field": "amount", "operator": "<", "value": 50000}
],
"scoreImpact": -10.0,
"flags": ["LOW_VALUE"],
"reasons": ["Low value order detected, potential risk"]
}'
);

INSERT INTO scoring_rule (rule_json)
SELECT '{
"ruleId": "returning_customer_boost",
"eventType": "ORDER",
"conditions": [
 {"field": "customerType", "operator": "==", "value": "RETURNING"}
],
"scoreImpact": 2.5,
"confidenceImpact": 0.0,
"flags": ["LOYAL_CUSTOMER"],
"reasons": ["Returning customer, lower risk"]
}'
WHERE NOT EXISTS (
 SELECT 1 FROM scoring_rule 
 WHERE rule_json = '{
"ruleId": "returning_customer_boost",
"eventType": "ORDER",
"conditions": [
 {"field": "customerType", "operator": "==", "value": "RETURNING"}
],
"scoreImpact": 2.5,
"confidenceImpact": 0.0,
"flags": ["LOYAL_CUSTOMER"],
"reasons": ["Returning customer, lower risk"]
}'
);

INSERT INTO scoring_rule (rule_json)
SELECT '{
"ruleId": "high_value_transaction",
"eventType": "TRANSACTION",
"conditions": [
 {"field": "amount", "operator": ">", "value": 10000}
],
"scoreImpact": 15.0,
"confidenceImpact": 0.05,
"flags": ["HIGH_VALUE"],
"reasons": ["High value transaction detected"]
}'
WHERE NOT EXISTS (
 SELECT 1 FROM scoring_rule 
 WHERE rule_json = '{
"ruleId": "high_value_transaction",
"eventType": "TRANSACTION",
"conditions": [
 {"field": "amount", "operator": ">", "value": 10000}
],
"scoreImpact": 15.0,
"confidenceImpact": 0.05,
"flags": ["HIGH_VALUE"],
"reasons": ["High value transaction detected"]
}'
);

INSERT INTO scoring_rule (rule_json)
SELECT '{
"ruleId": "upi_channel_risk",
"eventType": "TRANSACTION",
"conditions": [
 {"field": "channel", "operator": "==", "value": "UPI"}
],
"scoreImpact": -5.0,
"flags": ["CHANNEL_RISK"],
"reasons": ["UPI channel used, additional risk applied"]
}'
WHERE NOT EXISTS (
 SELECT 1 FROM scoring_rule 
 WHERE rule_json = '{
"ruleId": "upi_channel_risk",
"eventType": "TRANSACTION",
"conditions": [
 {"field": "channel", "operator": "==", "value": "UPI"}
],
"scoreImpact": -5.0,
"flags": ["CHANNEL_RISK"],
"reasons": ["UPI channel used, additional risk applied"]
}'
);

