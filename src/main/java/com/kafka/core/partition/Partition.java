package com.kafka.core.partition;

import com.kafka.core.message.KafkaMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Partition - A single ordered log of messages within a Topic
 * 
 * Think of a Partition like:
 * - A single file where you can only append to the end
 * - A queue where messages are added to the back
 * - A numbered list where each item gets an index (offset)
 * 
 * Key responsibilities:
 * 1. Store messages in order (append-only)
 * 2. Assign sequential offsets (0, 1, 2, 3...)
 * 3. Allow reading messages by offset
 * 4. Maintain message ordering
 * 
 * ✅ IMPLEMENTED: All core functionality complete!
 */
public class Partition {

    public Partition(int partitionId) {
        this.id = partitionId;
        this.messages = new ArrayList<>();  // Java 7+ Diamond operator
        this.nextOffset = 0;
    }

    private final int id;
    private final List<KafkaMessage> messages;
    private long nextOffset = 0;

    /* A lock that guards all modifications to the partition - EXACTLY like real Kafka! */
    private final Object lock = new Object();

    public long getLatestOffset() {
        synchronized (lock) {  // Thread-safe access to nextOffset
            return messages.isEmpty() ? 0 : nextOffset;  // Ternary operator - more concise
        }
    }

    public int getId() {
        return this.id;  // ID never changes, no synchronization needed
    }

    public int size() {  // Use primitive int instead of Integer wrapper
        synchronized (lock) {  // Thread-safe access to messages.size()
            return messages.size();  // 'this.' is optional for clarity
        }
    }

    public boolean isEmpty() {  // Use primitive boolean instead of Boolean wrapper
        synchronized (lock) {  // Thread-safe access to messages.isEmpty()
            return messages.isEmpty();
        }
    }

    /**
     * Appends a message to this partition and returns the assigned offset
     *
     * Thread-safe implementation using the same pattern as real Kafka UnifiedLog.append()
     *
     * @param message The message to append
     * @return The offset assigned to this message
     */
    public long append(KafkaMessage message) {
        // EXACTLY like real Kafka: synchronized (lock) guards all modifications!
        synchronized (lock) {
            messages.add(message);
            return nextOffset++;  // Post-increment: returns current value, then increments
        }
    }

    /**
     * Reads a message at the specified offset
     *
     * Thread-safe implementation - prevents reading during writes
     *
     * @param offset The offset to read from
     * @return The message at that offset
     */
    public KafkaMessage read(long offset) {
        synchronized (lock) {  // Prevent reading during writes
            if (offset < 0 || offset >= messages.size()) {
                throw new IllegalArgumentException("Offset %d is out of bounds".formatted(offset));  // Java 15+ String.formatted()
            }
            return messages.get((int) offset);
        }
    }

    public List<KafkaMessage> readRange(int startOffset, int endOffset) {  // Better parameter names
        // Add bounds checking for safety
        if (startOffset < 0 || endOffset >= messages.size() || startOffset > endOffset) {
            throw new IllegalArgumentException("Invalid range: [%d, %d]".formatted(startOffset, endOffset));
        }
        return messages.subList(startOffset, endOffset + 1);  // +1 because subList is exclusive
    }

}
