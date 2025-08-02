package com.kafka.core.topic;

import com.kafka.core.message.KafkaMessage;
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
        // TODO: Implement Topic class with constructor
        // Topic topic = new Topic(topicName);
        
        // Then: Topic should have the name and default partitions
        // TODO: Add assertions
        // assertEquals(topicName, topic.getName());
        // assertEquals(1, topic.getPartitionCount()); // Default to 1 partition
        
        fail("TODO: Implement Topic class first");
    }
    
    @Test
    @DisplayName("Should create topic with specified partition count")
    void testCreateTopicWithPartitions() {
        // Given: I want a topic with multiple partitions for better performance
        String topicName = "high-volume-events";
        int partitionCount = 3;
        
        // When: I create the topic with specific partition count
        // TODO: Implement Topic constructor with partition count
        // Topic topic = new Topic(topicName, partitionCount);
        
        // Then: Topic should have the specified partitions
        // TODO: Add assertions
        // assertEquals(topicName, topic.getName());
        // assertEquals(partitionCount, topic.getPartitionCount());
        
        fail("TODO: Implement Topic class with partition count constructor");
    }
    
    @Test
    @DisplayName("Should send message to correct partition based on key")
    void testSendMessageToPartition() {
        // Given: A topic with 3 partitions
        String topicName = "user-events";
        int partitionCount = 3;
        // Topic topic = new Topic(topicName, partitionCount);
        
        // And: A message with a key
        KafkaMessage message = KafkaMessage.builder()
            .key("user-123")
            .value("User logged in")
            .build();
        
        // When: I send the message to the topic
        // TODO: Implement send method
        // topic.send(message);
        
        // Then: Message should go to a specific partition based on key hash
        // TODO: Implement partition selection logic
        // int expectedPartition = Math.abs("user-123".hashCode()) % partitionCount;
        // assertTrue(topic.getPartition(expectedPartition).contains(message));
        
        fail("TODO: Implement Topic.send() method and partition logic");
    }
    
    @Test
    @DisplayName("Should send message with null key to random partition")
    void testSendMessageWithNullKey() {
        // Given: A topic with 3 partitions
        // Topic topic = new Topic("events", 3);
        
        // And: A message without a key
        KafkaMessage message = KafkaMessage.builder()
            .value("Anonymous event")
            .build();
        
        // When: I send the message
        // TODO: Implement null key handling
        // topic.send(message);
        
        // Then: Message should go to some partition (round-robin or random)
        // TODO: Verify message was stored somewhere
        // assertEquals(1, topic.getTotalMessageCount());
        
        fail("TODO: Implement null key handling in Topic.send()");
    }
    
    @Test
    @DisplayName("Should maintain message order within same partition")
    void testMessageOrderingWithinPartition() {
        // Given: A topic with multiple partitions
        // Topic topic = new Topic("ordered-events", 2);
        
        // When: I send multiple messages with the same key
        String sameKey = "user-456";
        KafkaMessage msg1 = KafkaMessage.builder().key(sameKey).value("Event 1").build();
        KafkaMessage msg2 = KafkaMessage.builder().key(sameKey).value("Event 2").build();
        KafkaMessage msg3 = KafkaMessage.builder().key(sameKey).value("Event 3").build();
        
        // TODO: Send messages
        // topic.send(msg1);
        // topic.send(msg2);
        // topic.send(msg3);
        
        // Then: All messages should be in the same partition in order
        // TODO: Verify ordering
        // int partition = Math.abs(sameKey.hashCode()) % 2;
        // List<KafkaMessage> messages = topic.getPartition(partition).getMessages();
        // assertEquals(3, messages.size());
        // assertEquals("Event 1", messages.get(0).getValue());
        // assertEquals("Event 2", messages.get(1).getValue());
        // assertEquals("Event 3", messages.get(2).getValue());
        
        fail("TODO: Implement message ordering within partitions");
    }
    
    // TODO: Add more tests for:
    // - Invalid topic names (empty, null, special characters)
    // - Invalid partition counts (0, negative)
    // - Getting partition by index
    // - Topic statistics (total messages, messages per partition)
    
    // Questions to think about:
    // 1. What makes a good topic name?
    // 2. How many partitions should a topic have?
    // 3. What happens if you change partition count later?
    // 4. How does partition assignment affect performance?
}
