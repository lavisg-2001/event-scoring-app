CREATE DATABASE IF NOT EXISTS event_scoring;
USE event_scoring;

CREATE TABLE IF NOT EXISTS scoring_rule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rule_json TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS event_response (
    event_id VARCHAR(255) PRIMARY KEY,
    response_json TEXT NOT NULL
);

-- Insert the sample rule only if it doesn't already exist (checks by rule_json)
--INSERT INTO scoring_rule (rule_json)
--SELECT '{"ruleId":"high_value_order","eventType":"ORDER","conditions":[{"field":"amount","operator":">","value":100000},{"field":"customerType","operator":"==","value":"NEW"}],"scoreImpact":20.0,"confidenceImpact":0.1,"flags":["HIGH_VALUE"],"reasons":["High value order detected","New customer risk applied"]}'
--WHERE NOT EXISTS (
--    SELECT 1 FROM scoring_rule
--    WHERE rule_json = '{"ruleId":"high_value_order","eventType":"ORDER","conditions":[{"field":"amount","operator":">","value":100000},{"field":"customerType","operator":"==","value":"NEW"}],"scoreImpact":20.0,"confidenceImpact":0.1,"flags":["HIGH_VALUE"],"reasons":["High value order detected","New customer risk applied"]}'
--);