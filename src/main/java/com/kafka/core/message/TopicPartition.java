package com.kafka.core.message;

import java.util.Objects;

/**
 * Represents a topic-partition pair in Kafka.
 * 
 * Learning Objectives:
 * 1. Understand value objects and their characteristics
 * 2. Learn proper equals/hashCode implementation for use as map keys
 * 3. Practice with immutable design patterns
 * 4. Understand why this class is critical for Kafka's architecture
 * 
 * Key Kafka Concepts:
 * - Topics are logical channels for messages
 * - Each topic is divided into partitions for scalability
 * - TopicPartition uniquely identifies a partition within a topic
 * - Used as keys in maps for offset tracking, consumer assignment, etc.
 * - Must have proper equals/hashCode to work correctly in collections
 */
public final class TopicPartition implements Comparable<TopicPartition> {
    
    // TODO: Implement these fields
    // Why are they final? What does that guarantee?
    
    private final String topic;
    private final int partition;
    
    // TODO: Implement constructor
    public TopicPartition(String topic, int partition) {
        // TODO: Add validation
        // 1. Topic cannot be null or empty
        // 2. Partition must be non-negative
        // 3. Consider topic naming rules (Kafka has specific rules)
        
        this.topic = null; // TODO: Implement with validation
        this.partition = -1; // TODO: Implement with validation
    }
    
    // TODO: Implement getters
    public String getTopic() {
        return topic;
    }
    
    public int getPartition() {
        return partition;
    }
    
    // TODO: Implement equals method
    // This is CRITICAL - TopicPartition is used as a key in maps
    // Two TopicPartitions are equal if they have the same topic and partition
    @Override
    public boolean equals(Object obj) {
        // TODO: Implement proper equals method
        // 1. Check for same reference (==)
        // 2. Check for null and class type
        // 3. Compare topic and partition fields
        // 4. Use Objects.equals() for null-safe string comparison
        
        return false; // Replace with implementation
    }
    
    // TODO: Implement hashCode method
    // MUST be consistent with equals - if two objects are equal, they must have same hash code
    @Override
    public int hashCode() {
        // TODO: Implement using Objects.hash()
        // Include both topic and partition in hash calculation
        
        return 0; // Replace with implementation
    }
    
    // TODO: Implement toString for debugging
    @Override
    public String toString() {
        // TODO: Return readable format like "topic-partition-0"
        // This is useful for logging and debugging
        
        return null; // Replace with implementation
    }
    
    // TODO: Implement Comparable interface
    // This allows sorting TopicPartitions in collections
    // Useful for consistent ordering in consumer assignment
    @Override
    public int compareTo(TopicPartition other) {
        // TODO: Implement comparison logic
        // 1. First compare by topic name (alphabetical)
        // 2. If topics are equal, compare by partition number
        // 3. Use String.compareTo() and Integer.compare()
        
        return 0; // Replace with implementation
    }
    
    // TODO: Add utility methods
    
    /**
     * Creates a TopicPartition from a string representation.
     * Expected format: "topic-partition-N" where N is the partition number.
     */
    public static TopicPartition fromString(String str) {
        // TODO: Parse string format "topic-partition-N"
        // 1. Validate input is not null/empty
        // 2. Find last occurrence of "-partition-"
        // 3. Extract topic name and partition number
        // 4. Handle parsing errors gracefully
        
        return null; // Replace with implementation
    }
    
    /**
     * Validates if a topic name follows Kafka naming rules.
     */
    public static boolean isValidTopicName(String topic) {
        // TODO: Implement Kafka topic naming validation
        // Kafka rules:
        // 1. Cannot be null or empty
        // 2. Cannot be "." or ".."
        // 3. Cannot contain characters: / \ , : " ' ` ? * | < > space
        // 4. Length must be <= 249 characters
        // 5. Cannot start with underscore (reserved for internal topics)
        
        return false; // Replace with implementation
    }
    
    /**
     * Creates a range of TopicPartitions for a topic.
     * Useful for creating all partitions for a topic.
     */
    public static TopicPartition[] range(String topic, int numPartitions) {
        // TODO: Create array of TopicPartitions from 0 to numPartitions-1
        // Validate inputs (topic not null, numPartitions > 0)
        
        return null; // Replace with implementation
    }
}

/**
 * Learning Notes:
 * 
 * 1. Value Objects:
 *    - Defined by their values, not identity
 *    - Two objects with same values are considered equal
 *    - Usually immutable
 *    - Used as keys in maps and elements in sets
 * 
 * 2. equals() and hashCode() Contract:
 *    - If a.equals(b) then a.hashCode() == b.hashCode()
 *    - If a.hashCode() != b.hashCode() then !a.equals(b)
 *    - hashCode() should be consistent (same value across calls)
 *    - equals() should be reflexive, symmetric, transitive, consistent
 * 
 * 3. Why This Class Matters in Kafka:
 *    - Used as keys in offset maps: Map<TopicPartition, Long>
 *    - Used in consumer assignment: Map<Consumer, Set<TopicPartition>>
 *    - Used in producer metadata: Map<TopicPartition, PartitionInfo>
 *    - Critical for performance - bad hashCode() = slow maps
 * 
 * 4. Comparable Interface:
 *    - Allows natural ordering in sorted collections
 *    - Useful for consistent partition assignment
 *    - Should be consistent with equals()
 * 
 * 5. Validation Strategies:
 *    - Fail-fast: validate in constructor
 *    - Clear error messages for debugging
 *    - Consider what constitutes valid input
 * 
 * Common Mistakes to Avoid:
 * 1. Forgetting to implement hashCode() when implementing equals()
 * 2. Using mutable fields in equals/hashCode
 * 3. Not handling null values properly
 * 4. Inconsistent equals/hashCode implementations
 * 5. Not validating constructor inputs
 * 
 * Testing Strategy:
 * 1. Test equals/hashCode contract thoroughly
 * 2. Test as map keys (put/get operations)
 * 3. Test sorting behavior (Comparable)
 * 4. Test validation logic
 * 5. Test edge cases (empty strings, negative numbers)
 */
