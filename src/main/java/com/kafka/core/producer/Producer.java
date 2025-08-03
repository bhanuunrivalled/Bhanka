package com.kafka.core.producer;

import com.kafka.core.topic.Topic;
import com.kafka.core.broker.TopicRegistry;

import java.util.Set;

/**
 * Producer - Simple API for sending messages to topics
 * <p>
 * Think of Producer like:
 * - A post office where you drop off letters (messages)
 * - A restaurant where you place orders (send to kitchen/topic)
 * - A librarian who files books in the right section (topic/partition)
 * <p>
 * Key responsibilities:
 * 1. Provide simple send(topicName, key, value) method
 * 2. Automatically create topics if they don't exist
 * 3. Handle input validation and error cases
 * 4. Manage topic cache for efficiency
 * <p>
 * TODO: Implement this class step by step!
 */
public class Producer {

    private final TopicRegistry topicRegistry;

    public Producer() {
        // Connect to the topic registry (like connecting to Kafka broker)
        this.topicRegistry = TopicRegistry.getInstance();
    }

    public void send(String topicName, String messageKey, String messageValue) {
        // Step 1: Validate inputs
        if (topicName == null || topicName.trim().isEmpty()) {
            throw new IllegalArgumentException("Topic name cannot be null or empty");
        }
        if (messageValue == null || messageValue.trim().isEmpty()) {
            throw new IllegalArgumentException("Message value cannot be null or empty");
        }
        // Step 2: Get or create the topic
        Topic topic = getOrCreateTopic(topicName);
        // Step 3: Send the message to the topic
        topic.send(messageKey, messageValue);
    }

    private Topic getOrCreateTopic(String topicName) {
        return topicRegistry.getOrCreateTopic(topicName);
    }

    public boolean topicExists(String topicName) {
        if (topicName == null || topicName.isEmpty()) {
            throw new IllegalArgumentException("Topic name cannot be null or empty");
        }
        return topicRegistry.topicExists(topicName);
    }

    public Topic getTopic(String topicName) {
        return topicRegistry.getTopic(topicName);
    }

    public Set<String> getTopicNames() {
        return topicRegistry.getTopicNames();
    }
}
