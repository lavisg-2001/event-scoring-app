package com.example.score.beans;

import java.util.List;

public class EventResponse {
    private String eventId;
    private Double score;
    private Double confidence;
    private List<String> flags;
    private List<String> reasons;

    public EventResponse() {}

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
    public List<String> getFlags() { return flags; }
    public void setFlags(List<String> flags) { this.flags = flags; }
    public List<String> getReasons() { return reasons; }
    public void setReasons(List<String> reasons) { this.reasons = reasons; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String eventId;
        private Double score;
        private Double confidence;
        private List<String> flags;
        private List<String> reasons;

        public Builder eventId(String eventId) {
            this.eventId = eventId;
            return this;
        }

        public Builder score(Double score) {
            this.score = score;
            return this;
        }

        public Builder confidence(Double confidence) {
            this.confidence = confidence;
            return this;
        }

        public Builder flags(List<String> flags) {
            this.flags = flags;
            return this;
        }

        public Builder reasons(List<String> reasons) {
            this.reasons = reasons;
            return this;
        }

        public EventResponse build() {
            EventResponse response = new EventResponse();
            response.setEventId(this.eventId);
            response.setScore(this.score);
            response.setConfidence(this.confidence);
            response.setFlags(this.flags);
            response.setReasons(this.reasons);
            return response;
        }
    }
}