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
        Partition partition = new Partition(partitionId);

        // Then: Partition should be empty and have the ID
        assertEquals(partitionId, partition.getId());
        assertEquals(0, partition.size());
        assertTrue(partition.isEmpty());
        assertEquals(0L, partition.getLatestOffset()); // Next offset to be assigned
    }
    
    @Test
    @DisplayName("Should append message and assign offset")
    void testAppendMessage() {
        // Given: An empty partition
        Partition partition = new Partition(0);

        // And: A message to store
        KafkaMessage message = KafkaMessage.builder()
            .key("user-123")
            .value("First message")
            .build();

        // When: I append the message
        long offset = partition.append(message);

        // Then: Message should get offset 0 (first message)
        assertEquals(0L, offset);
        assertEquals(1, partition.size());
        assertFalse(partition.isEmpty());
        assertEquals(1L, partition.getLatestOffset()); // Next offset would be 1
    }
    
    @Test
    @DisplayName("Should assign sequential offsets to multiple messages")
    void testSequentialOffsets() {
        // Given: An empty partition
        Partition partition = new Partition(0);

        // When: I append multiple messages
        KafkaMessage msg1 = KafkaMessage.builder().value("Message 1").build();
        KafkaMessage msg2 = KafkaMessage.builder().value("Message 2").build();
        KafkaMessage msg3 = KafkaMessage.builder().value("Message 3").build();

        long offset1 = partition.append(msg1);
        long offset2 = partition.append(msg2);
        long offset3 = partition.append(msg3);

        // Then: Offsets should be sequential
        assertEquals(0L, offset1);
        assertEquals(1L, offset2);
        assertEquals(2L, offset3);
        assertEquals(3, partition.size());
        assertEquals(3L, partition.getLatestOffset()); // Next offset would be 3
    }
    
    @Test
    @DisplayName("Should read message by offset")
    void testReadMessage() {
        // Given: A partition with some messages
        Partition partition = new Partition(0);
        KafkaMessage originalMessage = KafkaMessage.builder()
            .key("test-key")
            .value("Test message")
            .build();

        // Append message
        long offset = partition.append(originalMessage);

        // When: I read the message by offset
        KafkaMessage retrievedMessage = partition.read(offset);

        // Then: Should get the same message back (this is why we need equals/hashCode!)
        assertEquals(originalMessage, retrievedMessage);
        assertEquals(originalMessage.getKey(), retrievedMessage.getKey());
        assertEquals(originalMessage.getValue(), retrievedMessage.getValue());
    }
    
    @Test
    @DisplayName("Should throw exception for invalid offset")
    void testReadInvalidOffset() {
        // Given: A partition with one message
        Partition partition = new Partition(0);
        partition.append(KafkaMessage.builder().value("Only message").build());

        // When/Then: Reading invalid offsets should throw exception
        assertThrows(IllegalArgumentException.class, () -> partition.read(-1));
        assertThrows(IllegalArgumentException.class, () -> partition.read(1)); // Only offset 0 exists
        assertThrows(IllegalArgumentException.class, () -> partition.read(999));

        // Valid read should work
        assertDoesNotThrow(() -> partition.read(0));
    }
    
    @Test
    @DisplayName("Should track partition size correctly")
    void testPartitionSize() {
        // Given: An empty partition
        Partition partition = new Partition(0);

        // When: Partition is empty
        assertEquals(0, partition.size());
        assertTrue(partition.isEmpty());

        // When: I add messages
        partition.append(KafkaMessage.builder().value("Message 1").build());
        assertEquals(1, partition.size());
        assertFalse(partition.isEmpty());

        partition.append(KafkaMessage.builder().value("Message 2").build());
        assertEquals(2, partition.size());
        assertFalse(partition.isEmpty());

        partition.append(KafkaMessage.builder().value("Message 3").build());
        assertEquals(3, partition.size());
        assertFalse(partition.isEmpty());
    }
    
    @Test
    @DisplayName("Should read range of messages")
    void testReadMessageRange() {
        // Given: A partition with multiple messages
        Partition partition = new Partition(0);

        // Add some test messages
        for (int i = 0; i < 5; i++) {
            KafkaMessage msg = KafkaMessage.builder()
                .value("Message " + i)
                .build();
            partition.append(msg);
        }

        // When: I read a range of messages
        java.util.List<KafkaMessage> messages = partition.readRange(1, 3); // Read offsets 1, 2, 3

        // Then: Should get the correct messages
        assertEquals(3, messages.size());
        assertEquals("Message 1", messages.get(0).getValue());
        assertEquals("Message 2", messages.get(1).getValue());
        assertEquals("Message 3", messages.get(2).getValue());
    }
}
