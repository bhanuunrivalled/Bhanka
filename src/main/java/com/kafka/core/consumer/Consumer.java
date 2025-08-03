package com.kafka.core.consumer;

import com.kafka.core.message.KafkaMessage;
import com.kafka.core.topic.Topic;
import com.kafka.core.broker.TopicRegistry;

/**
 * Consumer - Simple API for reading messages from topics
 * 
 * Think of Consumer like:
 * - A mailbox reader who checks mail in order
 * - A book reader who reads pages sequentially
 * - A queue processor who handles tasks one by one
 * 
 * Key responsibilities:
 * 1. Provide iterator-style interface (hasNext/next)
 * 2. Read messages sequentially across all partitions
 * 3. Track current reading position automatically
 * 4. Handle empty topics and end-of-data gracefully
 * 
 * TODO: Implement this class step by step!
 */
public class Consumer {

    private final Topic topic;
    private int currentPartition = 0;
    private long currentOffset = 0;

    public Consumer(String topicName) {
        if (topicName == null || topicName.trim().isEmpty()) {
            throw new IllegalArgumentException("Topic name cannot be null or empty");
        }

        // Connect to topic registry (like connecting to Kafka broker)
        TopicRegistry topicRegistry = TopicRegistry.getInstance();
        if (!topicRegistry.topicExists(topicName)) {
            throw new IllegalArgumentException("Topic does not exist: " + topicName);
        }

        this.topic = topicRegistry.getTopic(topicName);
    }

    public boolean hasNext() {
        // Check if we're past all partitions
        if (currentPartition >= topic.getPartitionCount()) {
            return false;
        }

        // Check if current partition has more messages
        if (currentOffset < topic.getPartition(currentPartition).size()) {
            return true;
        }

        // Check if there are more partitions with messages
        for (int i = currentPartition + 1; i < topic.getPartitionCount(); i++) {
            if (topic.getPartition(i).size() > 0) {
                return true;
            }
        }

        return false;
    }

    public KafkaMessage next() {
        if (!hasNext()) {
            throw new IllegalStateException("No more messages available");
        }

        // Read message from current position
        KafkaMessage message = topic.getPartition(currentPartition).read(currentOffset);

        // Move to next position
        moveToNextMessage();

        return message;
    }

    private void moveToNextMessage() {
        currentOffset++;

        // If we've reached the end of current partition, move to next partition
        if (currentOffset >= topic.getPartition(currentPartition).size()) {
            currentPartition++;
            currentOffset = 0;
        }
    }
}
