package com.kafka.core.topic;

import com.kafka.core.message.KafkaMessage;
import com.kafka.core.partition.Partition;

/**
 * Topic - A category or feed name to which messages are sent
 * 
 * Think of a Topic like:
 * - A folder containing multiple files (partitions)
 * - A Slack channel that can have multiple threads
 * - A database table that's split across multiple files for performance
 * 
 * Key responsibilities:
 * 1. Manage multiple partitions
 * 2. Route messages to the correct partition based on key
 * 3. Provide access to partitions for reading
 * 
 * TODO: Implement this class step by step!
 */
public class Topic {
    
    // TODO: Add fields
    // - String name: The topic name
    // - List<Partition> partitions: The partitions in this topic
    // - int partitionCount: Number of partitions (for convenience)
    
    // TODO: Constructor with just name (default to 1 partition)
    // public Topic(String name) {
    //     // Initialize with 1 partition
    // }
    
    // TODO: Constructor with name and partition count
    // public Topic(String name, int partitionCount) {
    //     // Validate inputs
    //     // Create the specified number of partitions
    // }
    
    // TODO: Getter methods
    // public String getName() { ... }
    // public int getPartitionCount() { ... }
    // public Partition getPartition(int index) { ... }
    
    // TODO: Core method - send message to appropriate partition
    // public void send(KafkaMessage message) {
    //     // If message has key: use hash to determine partition
    //     // If message has no key: use round-robin or random
    //     // Then append to the chosen partition
    // }
    
    // TODO: Utility methods
    // public int getTotalMessageCount() { ... }
    // public long getTotalMessages() { ... }
    
    // TODO: Helper method to calculate partition for a key
    // private int calculatePartition(String key) {
    //     // Use key.hashCode() % partitionCount
    //     // Handle negative hash codes properly
    // }
    
    // Questions to think about while implementing:
    // 1. What should happen if name is null or empty?
    // 2. What should happen if partitionCount is 0 or negative?
    // 3. How do you handle negative hash codes?
    // 4. Should you allow changing partition count after creation?
}
