package com.kafka.core.message;

import java.util.Objects;

/**
 * KafkaMessage - A simple representation of a Kafka message
 * 
 * Let's start with what you know:
 * 1. Messages go to topics
 * 2. Messages have a key that determines partitioning  
 * 3. Messages have content (value)
 * 
 * We'll add more complexity later as we learn!
 */
public class KafkaMessage {
    
    // Core fields - keeping it simple for now
    private final String key;           // Optional - determines which partition this message goes to
    private final String value;         // Required - the actual message content
    
    // Constructor - private because we'll use Builder pattern
    private KafkaMessage(String key, String value) {
        // FIXME what happens if the user creates the kafak message form the Constructor
        // Question: What should happen if value is null? Should we allow it?
        // Question: What does it mean if key is null? How does partitioning work then?
        this.key = key;
        this.value = value;
    }
    
    // Getters
    public String getKey() {
        return key;
    }
    
    public String getValue() {
        return value;
    }
    
    // Builder pattern - makes it easy to create messages
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String key;
        private String value;
        
        public Builder key(String key) {
            this.key = key;
            return this;
        }
        
        public Builder value(String value) {
            this.value = value;
            return this;
        }
        
        public KafkaMessage build() {
            // 1. Should we allow null values? The tests expect IllegalArgumentException
            // 2. Should we allow empty string values? The tests expect IllegalArgumentException
            // 3. Use this pattern: if (condition) throw new IllegalArgumentException("message");
            // 4. What about null keys? Should they be allowed? (Hint: yes, for random partitioning)

            if (value == null) {
                throw new IllegalArgumentException("Value cannot be null");
            }
            if (value.isEmpty()) {
                throw new IllegalArgumentException("Value cannot be empty");
            }
            return new KafkaMessage(key, value);
        }
    }
    
    // Helper methods for debugging
    @Override
    public String toString() {
        return String.format("KafkaMessage{key='%s', value='%s'}", key, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj instanceof KafkaMessage) {
            KafkaMessage other = (KafkaMessage) obj;
            return Objects.equals(key, other.key) && Objects.equals(value, other.value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }
    
}
