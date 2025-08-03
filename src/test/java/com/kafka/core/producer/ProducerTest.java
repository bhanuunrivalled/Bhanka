package com.kafka.core.producer;

import com.kafka.core.message.KafkaMessage;
import com.kafka.core.topic.Topic;
import com.kafka.core.broker.TopicRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Producer - Simple API for sending messages to topics
 * 
 * Producer should:
 * 1. Provide simple send(topicName, key, value) method
 * 2. Automatically create topics if they don't exist
 * 3. Handle multiple messages to same/different topics
 * 4. Validate inputs and provide clear error messages
 */
public class ProducerTest {

    private Producer producer;

    @BeforeEach
    void setUp() {
        TopicRegistry.getInstance().clearAllTopics(); // Clear topics between tests
        producer = new Producer();
    }

    @Test
    @DisplayName("Should send message to topic")
    void testSendMessage() {
        // Given: A producer and message details
        String topicName = "user-events";
        String messageKey = "user-123";
        String messageValue = "User logged in";

        // When: I send a message
        producer.send(topicName, messageKey, messageValue);

        // Then: Topic should be created and contain the message
        assertTrue(producer.topicExists(topicName));
        Topic topic = producer.getTopic(topicName);
        assertEquals(1, topic.getTotalMessageCount());
        
        // Verify message content
        KafkaMessage storedMessage = topic.getPartition(0).read(0);
        assertEquals(messageKey, storedMessage.getKey());
        assertEquals(messageValue, storedMessage.getValue());
    }

    @Test
    @DisplayName("Should automatically create topic if it doesn't exist")
    void testSendToNewTopic() {
        // Given: A topic that doesn't exist yet
        String topicName = "new-topic";
        assertFalse(producer.topicExists(topicName));

        // When: I send a message to it
        producer.send(topicName, "key1", "value1");

        // Then: Topic should be created automatically
        assertTrue(producer.topicExists(topicName));
        Topic topic = producer.getTopic(topicName);
        assertNotNull(topic);
        assertEquals(topicName, topic.getName());
        assertEquals(1, topic.getTotalMessageCount());
    }

    @Test
    @DisplayName("Should handle multiple messages to same topic")
    void testSendMultipleMessages() {
        // Given: A topic name and multiple messages
        String topicName = "events";

        // When: I send multiple messages
        producer.send(topicName, "key1", "value1");
        producer.send(topicName, "key2", "value2");
        producer.send(topicName, "key3", "value3");

        // Then: All messages should be stored
        Topic topic = producer.getTopic(topicName);
        assertEquals(3, topic.getTotalMessageCount());
        
        // Verify messages are distributed across partitions based on key hash
        boolean foundMessage1 = false, foundMessage2 = false, foundMessage3 = false;
        
        for (int i = 0; i < topic.getPartitionCount(); i++) {
            for (int j = 0; j < topic.getPartition(i).size(); j++) {
                KafkaMessage msg = topic.getPartition(i).read(j);
                if ("value1".equals(msg.getValue())) foundMessage1 = true;
                if ("value2".equals(msg.getValue())) foundMessage2 = true;
                if ("value3".equals(msg.getValue())) foundMessage3 = true;
            }
        }
        
        assertTrue(foundMessage1);
        assertTrue(foundMessage2);
        assertTrue(foundMessage3);
    }

    @Test
    @DisplayName("Should handle messages with null keys")
    void testSendWithNullKey() {
        // Given: A topic and messages with null keys
        String topicName = "events";

        // When: I send messages with null keys
        producer.send(topicName, null, "anonymous1");
        producer.send(topicName, null, "anonymous2");
        producer.send(topicName, null, "anonymous3");

        // Then: Messages should be distributed round-robin
        Topic topic = producer.getTopic(topicName);
        assertEquals(3, topic.getTotalMessageCount());
        
        // Verify all messages are stored (round-robin distribution)
        int totalMessages = 0;
        for (int i = 0; i < topic.getPartitionCount(); i++) {
            totalMessages += topic.getPartition(i).size();
        }
        assertEquals(3, totalMessages);
    }

    @Test
    @DisplayName("Should validate inputs and throw appropriate exceptions")
    void testInputValidation() {
        // Test null topic name
        assertThrows(IllegalArgumentException.class, () -> {
            producer.send(null, "key", "value");
        });

        // Test empty topic name
        assertThrows(IllegalArgumentException.class, () -> {
            producer.send("", "key", "value");
        });

        // Test whitespace-only topic name
        assertThrows(IllegalArgumentException.class, () -> {
            producer.send("   ", "key", "value");
        });

        // Test null value
        assertThrows(IllegalArgumentException.class, () -> {
            producer.send("topic", "key", null);
        });

        // Test empty value
        assertThrows(IllegalArgumentException.class, () -> {
            producer.send("topic", "key", "");
        });

        // Null key should be allowed (round-robin routing)
        assertDoesNotThrow(() -> {
            producer.send("topic", null, "valid value");
        });
    }

    @Test
    @DisplayName("Should provide utility methods for topic management")
    void testTopicUtilities() {
        // Given: No topics initially
        assertTrue(producer.getTopicNames().isEmpty());

        // When: I send messages to different topics
        producer.send("topic1", "key", "value");
        producer.send("topic2", "key", "value");
        producer.send("topic1", "key2", "value2"); // Same topic again

        // Then: Should track topic names correctly
        assertEquals(2, producer.getTopicNames().size());
        assertTrue(producer.getTopicNames().contains("topic1"));
        assertTrue(producer.getTopicNames().contains("topic2"));
        
        assertTrue(producer.topicExists("topic1"));
        assertTrue(producer.topicExists("topic2"));
        assertFalse(producer.topicExists("nonexistent"));

        // Should be able to get topics
        Topic topic1 = producer.getTopic("topic1");
        Topic topic2 = producer.getTopic("topic2");
        assertNotNull(topic1);
        assertNotNull(topic2);
        assertEquals("topic1", topic1.getName());
        assertEquals("topic2", topic2.getName());
    }
}
