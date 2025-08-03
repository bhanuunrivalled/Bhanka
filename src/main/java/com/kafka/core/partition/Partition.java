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

    public long getLatestOffset() {
        return messages.isEmpty() ? 0 : nextOffset;  // Ternary operator - more concise
    }

    public int getId() {
        return this.id;
    }

    public int size() {  // Use primitive int instead of Integer wrapper
        return messages.size();  // 'this.' is optional for clarity
    }

    public boolean isEmpty() {  // Use primitive boolean instead of Boolean wrapper
        return messages.isEmpty();
    }

    public long append(KafkaMessage message) {
        messages.add(message);
        return nextOffset++;  // Post-increment: returns current value, then increments
    }

    public KafkaMessage read(long offset) {
        if (offset < 0 || offset >= messages.size()) {
            throw new IllegalArgumentException("Offset %d is out of bounds".formatted(offset));  // Java 15+ String.formatted()
        }
        return messages.get((int) offset);
    }

    public List<KafkaMessage> readRange(int startOffset, int endOffset) {  // Better parameter names
        // Add bounds checking for safety
        if (startOffset < 0 || endOffset >= messages.size() || startOffset > endOffset) {
            throw new IllegalArgumentException("Invalid range: [%d, %d]".formatted(startOffset, endOffset));
        }
        return messages.subList(startOffset, endOffset + 1);  // +1 because subList is exclusive
    }

}
