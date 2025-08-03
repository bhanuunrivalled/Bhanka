package com.kafka.core.broker;

import com.kafka.core.topic.Topic;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * TopicRegistry - Simulates a Kafka Broker's topic storage
 * 
 * In real Kafka, this would be the broker that:
 * - Stores all topics and their partitions
 * - Manages topic metadata
 * - Handles topic creation/deletion
 * - Provides topics to both producers and consumers
 * 
 * This is the "server" that both Producer and Consumer connect to.
 */
public class TopicRegistry {
    
    // Singleton pattern - one registry for the entire system (like one Kafka cluster)
    private static final TopicRegistry INSTANCE = new TopicRegistry();
    private final Map<String, Topic> topics = new HashMap<>();
    
    private TopicRegistry() {
        // Private constructor for singleton
    }
    
    public static TopicRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Get existing topic or create new one (like Kafka auto-creation)
     */
    public Topic getOrCreateTopic(String topicName) {
        return topics.computeIfAbsent(topicName, Topic::new);
    }
    
    /**
     * Get existing topic (throws if doesn't exist)
     */
    public Topic getTopic(String topicName) {
        Topic topic = topics.get(topicName);
        if (topic == null) {
            throw new IllegalArgumentException("Topic does not exist: " + topicName);
        }
        return topic;
    }
    
    /**
     * Check if topic exists
     */
    public boolean topicExists(String topicName) {
        return topics.containsKey(topicName);
    }
    
    /**
     * Get all topic names
     */
    public Set<String> getTopicNames() {
        return topics.keySet();
    }
    
    /**
     * Clear all topics (for testing)
     */
    public void clearAllTopics() {
        topics.clear();
    }
}
