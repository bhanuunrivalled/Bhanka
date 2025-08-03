package com.kafka.core.topic;

import com.kafka.core.message.KafkaMessage;
import com.kafka.core.partition.Partition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Topic - the next Kafka concept to learn!
 * 
 * A Topic is like a category or feed name to which messages are sent.
 * Think of it like:
 * - A folder that contains messages
 * - A channel in Slack/Discord
 * - A table in a database
 * 
 * Key concepts to implement:
 * 1. A topic has a name
 * 2. A topic has multiple partitions (for parallel processing)
 * 3. Messages are distributed across partitions based on their key
 */
public class TopicTest {
    
    @Test
    @DisplayName("Should create topic with name and default partition count")
    void testCreateTopicWithName() {
        // Given: I want to create a topic for user events
        String topicName = "user-events";

        // When: I create the topic
        Topic topic = new Topic(topicName);

        // Then: Topic should have the name and default partitions
        assertEquals(topicName, topic.getName());
        assertEquals(1, topic.getPartitionCount()); // Default to 1 partition
        assertNotNull(topic.getPartition(0)); // Should have one partition
    }
    
    @Test
    @DisplayName("Should create topic with specified partition count")
    void testCreateTopicWithPartitions() {
        // Given: I want a topic with multiple partitions for better performance
        String topicName = "high-volume-events";
        int partitionCount = 3;

        // When: I create the topic with specific partition count
        Topic topic = new Topic(topicName, partitionCount);

        // Then: Topic should have the specified partitions
        assertEquals(topicName, topic.getName());
        assertEquals(partitionCount, topic.getPartitionCount());

        // Verify all partitions exist
        for (int i = 0; i < partitionCount; i++) {
            assertNotNull(topic.getPartition(i));
            assertEquals(i, topic.getPartition(i).getId());
        }
    }
    
    @Test
    @DisplayName("Should send message to correct partition based on key")
    void testSendMessageToPartition() {
        // Given: A topic with 3 partitions
        String topicName = "user-events";
        int partitionCount = 3;
        Topic topic = new Topic(topicName, partitionCount);

        // And: A message key
        String messageKey = "user-123";
        String messageValue = "User logged in";

        // When: I send the message to the topic
        topic.send(messageKey, messageValue);

        // Then: Message should go to a specific partition based on key hash
        int expectedPartition = Math.abs(messageKey.hashCode()) % partitionCount;
        Partition targetPartition = topic.getPartition(expectedPartition);

        assertEquals(1, targetPartition.size());
        KafkaMessage storedMessage = targetPartition.read(0);
        assertEquals(messageKey, storedMessage.getKey());
        assertEquals(messageValue, storedMessage.getValue());
    }
    
    @Test
    @DisplayName("Should send message with null key using round-robin")
    void testSendMessageWithNullKey() {
        // Given: A topic with 3 partitions
        Topic topic = new Topic("events", 3);

        // When: I send multiple messages with null keys
        topic.send(null, "Anonymous event 1");
        topic.send(null, "Anonymous event 2");
        topic.send(null, "Anonymous event 3");
        topic.send(null, "Anonymous event 4");

        // Then: Messages should be distributed round-robin across partitions
        assertEquals(4, topic.getTotalMessageCount());

        // Verify round-robin distribution (should be 2, 1, 1 or similar even distribution)
        int partition0Count = topic.getPartition(0).size();
        int partition1Count = topic.getPartition(1).size();
        int partition2Count = topic.getPartition(2).size();

        // All partitions should have at least 1 message, and total should be 4
        assertTrue(partition0Count >= 1);
        assertTrue(partition1Count >= 1);
        assertTrue(partition2Count >= 1);
        assertEquals(4, partition0Count + partition1Count + partition2Count);
    }
    
    @Test
    @DisplayName("Should maintain message order within same partition")
    void testMessageOrderingWithinPartition() {
        // Given: A topic with multiple partitions
        Topic topic = new Topic("ordered-events", 2);

        // When: I send multiple messages with the same key
        String sameKey = "user-456";
        topic.send(sameKey, "Event 1");
        topic.send(sameKey, "Event 2");
        topic.send(sameKey, "Event 3");

        // Then: All messages should be in the same partition in order
        int expectedPartition = Math.abs(sameKey.hashCode()) % 2;
        Partition targetPartition = topic.getPartition(expectedPartition);

        assertEquals(3, targetPartition.size());
        assertEquals("Event 1", targetPartition.read(0).getValue());
        assertEquals("Event 2", targetPartition.read(1).getValue());
        assertEquals("Event 3", targetPartition.read(2).getValue());

        // Verify all messages have the same key
        assertEquals(sameKey, targetPartition.read(0).getKey());
        assertEquals(sameKey, targetPartition.read(1).getKey());
        assertEquals(sameKey, targetPartition.read(2).getKey());
    }
}

