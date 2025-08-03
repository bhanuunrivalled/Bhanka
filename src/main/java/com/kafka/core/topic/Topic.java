package com.kafka.core.topic;

import java.util.Arrays;
import java.util.List;

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
 */
public class Topic {

    private final String name;
    private final List<Partition> partitions;
    private final int partitionCount;
    private int roundRobinCounter = 0;

    public Topic(String topicName) {
        this.name = topicName;
        this.partitionCount = 1;
        this.partitions = List.of(new Partition(0));
    }


    public Topic(String name, int partitionCount) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Topic name cannot be null or empty");
        }
        if (partitionCount <= 0) {
            throw new IllegalArgumentException("Partition count must be positive");
        }
        this.name = name;
        this.partitionCount = partitionCount;
        this.partitions = createPartitions(partitionCount);
    }

    private List<Partition> createPartitions(int partitionCount2) {
        Partition[] partitions = new Partition[partitionCount2];
        for (int i = 0; i < partitionCount2; i++) {
            partitions[i] = new Partition(i);
        }
        return Arrays.asList(partitions);
    }

    public Partition getPartition(int index) {
        if (index < 0 || index >= partitions.size()) {
            throw new IllegalArgumentException("Partition index %d is out of bounds".formatted(index));
        }
        return partitions.get(index);
    }

    public String getName() {
        return name;
    }

    public int getPartitionCount() {
        return partitionCount;
    }

    public String toString() {
        return String.format("Topic{name='%s', partitionCount=%d}", name, partitionCount);
    }


    public void send(String sameKey, String string) {
        KafkaMessage message = KafkaMessage.builder()
        .key(sameKey)
        .value(string)
        .build();
        int partitionIndex = calculatePartition(sameKey);
        partitions.get(partitionIndex).append(message);
    }


    private int calculatePartition(String sameKey) {
        if (sameKey == null) {
            return roundRobinCounter++ % partitions.size();
        } else {
            return Math.abs(sameKey.hashCode()) % partitions.size();
        }
    }


    public Integer getTotalMessageCount() {
        return partitions.stream()
        .mapToInt(Partition::size)
        .sum();
    }

}
