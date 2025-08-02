package com.kafka.core.partition;

import com.kafka.core.message.KafkaMessage;
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
 * TODO: Implement this class step by step!
 */
public class Partition {
    
    // TODO: Add fields
    // - int id: The partition ID (0, 1, 2...)
    // - List<KafkaMessage> messages: Storage for messages (ArrayList is good choice)
    // - long nextOffset: The next offset to assign (starts at 0)
    
    // TODO: Constructor
    // public Partition(int id) {
    //     // Initialize fields
    //     // What should nextOffset start at?
    // }
    
    // TODO: Getter methods
    // public int getId() { ... }
    // public int size() { ... }
    // public boolean isEmpty() { ... }
    // public long getLatestOffset() { ... } // Return -1 if empty, otherwise last offset
    
    // TODO: Core method - append message and return its offset
    // public long append(KafkaMessage message) {
    //     // Add message to storage
    //     // Assign current nextOffset to this message
    //     // Increment nextOffset for next message
    //     // Return the offset assigned to this message
    // }
    
    // TODO: Core method - read message by offset
    // public KafkaMessage read(long offset) {
    //     // Validate offset is in bounds
    //     // Convert offset to array index
    //     // Return the message
    //     // Throw IndexOutOfBoundsException if invalid
    // }
    
    // TODO: Utility method - read range of messages
    // public List<KafkaMessage> readRange(long startOffset, long endOffset) {
    //     // Validate offsets
    //     // Return sublist of messages
    //     // Handle edge cases (empty range, invalid range)
    // }
    
    // TODO: Utility methods
    // public boolean containsOffset(long offset) { ... }
    // public long getEarliestOffset() { ... } // Always 0 for now
    // public List<KafkaMessage> getAllMessages() { ... } // For debugging
    
    // Questions to think about while implementing:
    // 1. What data structure is best for storing messages? Why?
    // 2. How do you convert an offset (long) to an array index (int)?
    // 3. What should happen if someone tries to read offset -1?
    // 4. What should happen if someone tries to read offset 999 when you only have 3 messages?
    // 5. How would you handle millions of messages? (Think about memory)
}
