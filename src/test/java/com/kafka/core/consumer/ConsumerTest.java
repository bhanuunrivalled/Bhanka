package com.kafka.core.consumer;

import com.kafka.core.message.KafkaMessage;
import com.kafka.core.producer.Producer;
import com.kafka.core.broker.TopicRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Consumer - Simple API for reading messages from topics
 * 
 * Consumer should:
 * 1. Provide iterator-style interface (hasNext/next)
 * 2. Read messages sequentially across all partitions
 * 3. Track current position automatically
 * 4. Handle empty topics gracefully
 */
public class ConsumerTest {

    private Producer producer;

    @BeforeEach
    void setUp() {
        TopicRegistry.getInstance().clearAllTopics(); // Clear topics between tests
        producer = new Producer();
    }

    @Test
    @DisplayName("Should read single message from topic")
    void testReadSingleMessage() {
        // Given: A topic with one message
        String topicName = "test-topic";
        producer.send(topicName, "key1", "Hello World");

        // When: I create a consumer and read
        Consumer consumer = new Consumer(topicName);
        
        // Then: Should be able to read the message
        assertTrue(consumer.hasNext());
        KafkaMessage message = consumer.next();
        
        assertEquals("key1", message.getKey());
        assertEquals("Hello World", message.getValue());
        
        // Should be no more messages
        assertFalse(consumer.hasNext());
    }

    @Test
    @DisplayName("Should read multiple messages in order")
    void testReadMultipleMessages() {
        // Given: A topic with multiple messages
        String topicName = "test-topic";
        producer.send(topicName, "user-1", "Message 1");
        producer.send(topicName, "user-2", "Message 2");
        producer.send(topicName, "user-3", "Message 3");

        // When: I create a consumer and read all messages
        Consumer consumer = new Consumer(topicName);
        
        // Then: Should read all messages
        assertTrue(consumer.hasNext());
        KafkaMessage msg1 = consumer.next();
        
        assertTrue(consumer.hasNext());
        KafkaMessage msg2 = consumer.next();
        
        assertTrue(consumer.hasNext());
        KafkaMessage msg3 = consumer.next();
        
        assertFalse(consumer.hasNext());

        // Verify message content (order may vary due to partitioning)
        String[] values = {msg1.getValue(), msg2.getValue(), msg3.getValue()};
        assertTrue(java.util.Arrays.asList(values).contains("Message 1"));
        assertTrue(java.util.Arrays.asList(values).contains("Message 2"));
        assertTrue(java.util.Arrays.asList(values).contains("Message 3"));
    }

    @Test
    @DisplayName("Should read messages from multiple partitions")
    void testReadFromMultiplePartitions() {
        // Given: A topic with messages that will go to different partitions
        String topicName = "multi-partition-topic";
        
        // Send enough messages to likely hit multiple partitions
        for (int i = 0; i < 10; i++) {
            producer.send(topicName, "key-" + i, "Message " + i);
        }

        // When: I read all messages
        Consumer consumer = new Consumer(topicName);
        int messageCount = 0;
        
        while (consumer.hasNext()) {
            KafkaMessage message = consumer.next();
            assertNotNull(message);
            assertNotNull(message.getValue());
            assertTrue(message.getValue().startsWith("Message "));
            messageCount++;
        }

        // Then: Should have read all 10 messages
        assertEquals(10, messageCount);
    }

    @Test
    @DisplayName("Should handle empty topic gracefully")
    void testEmptyTopic() {
        // Given: An empty topic
        String topicName = "empty-topic";
        producer.send(topicName, "key", "value"); // Create topic
        
        // Create consumer for the topic, but it only has 1 message
        Consumer consumer = new Consumer(topicName);
        
        // Read the one message
        assertTrue(consumer.hasNext());
        consumer.next();
        
        // Then: Should handle empty state gracefully
        assertFalse(consumer.hasNext());
        
        // Calling hasNext() multiple times should be safe
        assertFalse(consumer.hasNext());
        assertFalse(consumer.hasNext());
    }

    @Test
    @DisplayName("Should handle topic with only null-key messages")
    void testNullKeyMessages() {
        // Given: A topic with null-key messages (round-robin distribution)
        String topicName = "null-key-topic";
        producer.send(topicName, null, "Anonymous 1");
        producer.send(topicName, null, "Anonymous 2");
        producer.send(topicName, null, "Anonymous 3");

        // When: I read all messages
        Consumer consumer = new Consumer(topicName);
        int messageCount = 0;
        
        while (consumer.hasNext()) {
            KafkaMessage message = consumer.next();
            assertNull(message.getKey()); // All keys should be null
            assertTrue(message.getValue().startsWith("Anonymous "));
            messageCount++;
        }

        // Then: Should read all 3 messages
        assertEquals(3, messageCount);
    }

    @Test
    @DisplayName("Should validate constructor inputs")
    void testConstructorValidation() {
        // Test null topic name
        assertThrows(IllegalArgumentException.class, () -> {
            new Consumer(null);
        });

        // Test empty topic name
        assertThrows(IllegalArgumentException.class, () -> {
            new Consumer("");
        });

        // Test whitespace-only topic name
        assertThrows(IllegalArgumentException.class, () -> {
            new Consumer("   ");
        });

        // Test non-existent topic
        assertThrows(IllegalArgumentException.class, () -> {
            new Consumer("non-existent-topic");
        });
    }

    @Test
    @DisplayName("Should handle mixed key types (some null, some not)")
    void testMixedKeyTypes() {
        // Given: A topic with mixed key types
        String topicName = "mixed-keys";
        producer.send(topicName, "user-1", "Keyed message 1");
        producer.send(topicName, null, "Anonymous message");
        producer.send(topicName, "user-2", "Keyed message 2");

        // When: I read all messages
        Consumer consumer = new Consumer(topicName);
        int keyedMessages = 0;
        int nullKeyMessages = 0;
        
        while (consumer.hasNext()) {
            KafkaMessage message = consumer.next();
            if (message.getKey() == null) {
                nullKeyMessages++;
            } else {
                keyedMessages++;
            }
        }

        // Then: Should have correct counts
        assertEquals(2, keyedMessages);
        assertEquals(1, nullKeyMessages);
    }
}
