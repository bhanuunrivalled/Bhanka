package com.kafka.core.partition;

import com.kafka.core.message.KafkaMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Partition - the storage unit within a Topic
 * 
 * A Partition is like:
 * - A single file that stores messages in order
 * - A queue where messages are appended to the end
 * - A log where each message gets an offset (position number)
 * 
 * Key concepts to implement:
 * 1. Partitions store messages in order (append-only)
 * 2. Each message gets an offset (0, 1, 2, 3...)
 * 3. You can read messages by offset
 * 4. Partitions are the unit of parallelism in Kafka
 */
public class PartitionTest {
    
    @Test
    @DisplayName("Should create empty partition with ID")
    void testCreateEmptyPartition() {
        // Given: I want to create a partition
        int partitionId = 0;
        
        // When: I create the partition
        // TODO: Implement Partition class
        // Partition partition = new Partition(partitionId);
        
        // Then: Partition should be empty and have the ID
        // TODO: Add assertions
        // assertEquals(partitionId, partition.getId());
        // assertEquals(0, partition.size());
        // assertTrue(partition.isEmpty());
        
        fail("TODO: Implement Partition class");
    }
    
    @Test
    @DisplayName("Should append message and assign offset")
    void testAppendMessage() {
        // Given: An empty partition
        // Partition partition = new Partition(0);
        
        // And: A message to store
        KafkaMessage message = KafkaMessage.builder()
            .key("user-123")
            .value("First message")
            .build();
        
        // When: I append the message
        // TODO: Implement append method
        // long offset = partition.append(message);
        
        // Then: Message should get offset 0 (first message)
        // TODO: Add assertions
        // assertEquals(0L, offset);
        // assertEquals(1, partition.size());
        // assertFalse(partition.isEmpty());
        
        fail("TODO: Implement Partition.append() method");
    }
    
    @Test
    @DisplayName("Should assign sequential offsets to multiple messages")
    void testSequentialOffsets() {
        // Given: An empty partition
        // Partition partition = new Partition(0);
        
        // When: I append multiple messages
        KafkaMessage msg1 = KafkaMessage.builder().value("Message 1").build();
        KafkaMessage msg2 = KafkaMessage.builder().value("Message 2").build();
        KafkaMessage msg3 = KafkaMessage.builder().value("Message 3").build();
        
        // TODO: Append messages and check offsets
        // long offset1 = partition.append(msg1);
        // long offset2 = partition.append(msg2);
        // long offset3 = partition.append(msg3);
        
        // Then: Offsets should be sequential
        // TODO: Add assertions
        // assertEquals(0L, offset1);
        // assertEquals(1L, offset2);
        // assertEquals(2L, offset3);
        // assertEquals(3, partition.size());
        
        fail("TODO: Implement sequential offset assignment");
    }
    
    @Test
    @DisplayName("Should read message by offset")
    void testReadMessageByOffset() {
        // Given: A partition with some messages
        // Partition partition = new Partition(0);
        KafkaMessage originalMessage = KafkaMessage.builder()
            .key("test-key")
            .value("Test message")
            .build();
        
        // TODO: Append message
        // long offset = partition.append(originalMessage);
        
        // When: I read the message by offset
        // TODO: Implement read method
        // KafkaMessage retrievedMessage = partition.read(offset);
        
        // Then: Should get the same message back
        // TODO: Add assertions (this is why we need equals/hashCode!)
        // assertEquals(originalMessage.getKey(), retrievedMessage.getKey());
        // assertEquals(originalMessage.getValue(), retrievedMessage.getValue());
        
        fail("TODO: Implement Partition.read(offset) method");
    }
    
    @Test
    @DisplayName("Should throw exception for invalid offset")
    void testReadInvalidOffset() {
        // Given: A partition with one message
        // Partition partition = new Partition(0);
        // partition.append(KafkaMessage.builder().value("Only message").build());
        
        // When/Then: Reading invalid offsets should throw exception
        // TODO: Implement bounds checking
        // assertThrows(IndexOutOfBoundsException.class, () -> partition.read(-1));
        // assertThrows(IndexOutOfBoundsException.class, () -> partition.read(1));
        // assertThrows(IndexOutOfBoundsException.class, () -> partition.read(999));
        
        fail("TODO: Implement offset bounds checking");
    }
    
    @Test
    @DisplayName("Should read range of messages")
    void testReadMessageRange() {
        // Given: A partition with multiple messages
        // Partition partition = new Partition(0);
        
        // Add some test messages
        for (int i = 0; i < 5; i++) {
            KafkaMessage msg = KafkaMessage.builder()
                .value("Message " + i)
                .build();
            // partition.append(msg);
        }
        
        // When: I read a range of messages
        // TODO: Implement readRange method
        // List<KafkaMessage> messages = partition.readRange(1, 3); // Read offsets 1, 2, 3
        
        // Then: Should get the correct messages
        // TODO: Add assertions
        // assertEquals(3, messages.size());
        // assertEquals("Message 1", messages.get(0).getValue());
        // assertEquals("Message 2", messages.get(1).getValue());
        // assertEquals("Message 3", messages.get(2).getValue());
        
        fail("TODO: Implement Partition.readRange() method");
    }
    
    @Test
    @DisplayName("Should get latest offset")
    void testGetLatestOffset() {
        // Given: A partition with some messages
        // Partition partition = new Partition(0);
        
        // When: Partition is empty
        // TODO: Implement getLatestOffset
        // assertEquals(-1L, partition.getLatestOffset()); // -1 means no messages
        
        // When: I add messages
        // partition.append(KafkaMessage.builder().value("Msg 1").build());
        // partition.append(KafkaMessage.builder().value("Msg 2").build());
        
        // Then: Latest offset should be 1 (second message)
        // assertEquals(1L, partition.getLatestOffset());
        
        fail("TODO: Implement Partition.getLatestOffset() method");
    }
    
    // TODO: Add more tests for:
    // - Getting earliest offset (always 0 for now)
    // - Checking if partition contains specific offset
    // - Getting all messages (for debugging)
    // - Partition statistics (size, offset range)
    
    // Questions to think about:
    // 1. What data structure should we use to store messages? ArrayList? LinkedList?
    // 2. How do we handle very large partitions? (millions of messages)
    // 3. What happens if we run out of memory?
    // 4. How would real Kafka store this on disk?
}
