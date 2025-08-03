# Lesson 6: Producer/Consumer API Implementation Guide

## 🎯 What Exactly Are Producer/Consumer?

**Producer** and **Consumer** are **wrapper classes** that provide **clean, simple APIs** around your Topic/Partition infrastructure:

```
Application Layer:    [Producer] ←→ [Consumer]
                          ↓           ↑
API Layer:           [send()] ←→ [next()]
                          ↓           ↑
Infrastructure:      [Topic] → [Partition] → [KafkaMessage]
```

---

## 🏗️ Producer Architecture

### **Producer's Job: Make Sending Simple**
```java
// What applications want to write:
producer.send("user-events", "user-123", "User logged in");

// What Producer does internally:
// 1. Get/create topic "user-events"
// 2. Create KafkaMessage with key="user-123", value="User logged in"
// 3. Call topic.send(key, value)
// 4. Handle any errors
```

### **Producer Data Structure:**
```java
public class Producer {
    private final Map<String, Topic> topics;  // Topic cache
    private final int defaultPartitionCount;  // For new topics
    
    public Producer() {
        this.topics = new HashMap<>();
        this.defaultPartitionCount = 3;  // Reasonable default
    }
}
```

### **Core Producer Methods:**
```java
// Main sending method
public void send(String topicName, String key, String value) {
    // 1. Validate inputs
    // 2. Get or create topic
    // 3. Send message to topic
}

// Topic management
private Topic getOrCreateTopic(String topicName) {
    // 1. Check if topic exists in cache
    // 2. If not, create new topic with default partitions
    // 3. Add to cache and return
}

// Utility methods
public boolean topicExists(String topicName) { ... }
public Set<String> getTopicNames() { ... }
```

---

## 🏗️ Consumer Architecture

### **Consumer's Job: Make Reading Simple**
```java
// What applications want to write:
Consumer consumer = new Consumer("user-events");
while (consumer.hasNext()) {
    KafkaMessage message = consumer.next();
    processMessage(message);
}

// What Consumer does internally:
// 1. Track current partition and offset
// 2. Read message from current position
// 3. Advance to next position
// 4. Handle end-of-data gracefully
```

### **Consumer Data Structure:**
```java
public class Consumer {
    private final Topic topic;           // Topic to read from
    private int currentPartition;        // Which partition we're reading
    private long currentOffset;          // Which offset in that partition
    
    public Consumer(String topicName) {
        // Get topic (must exist)
        // Initialize position to start (partition 0, offset 0)
    }
}
```

### **Core Consumer Methods:**
```java
// Iterator-style interface
public boolean hasNext() {
    // Check if there are more messages to read
}

public KafkaMessage next() {
    // 1. Read message from current position
    // 2. Advance to next position
    // 3. Return message
}

// Position management
private void moveToNextMessage() {
    // 1. Increment offset
    // 2. If end of partition, move to next partition
    // 3. If end of all partitions, mark as finished
}
```

---

## 🧮 Consumer Position Tracking

### **The Challenge: Reading Across Multiple Partitions**
```
Topic "user-events" (3 partitions):
Partition 0: [msg0][msg1][msg2]
Partition 1: [msg3][msg4]
Partition 2: [msg5][msg6][msg7][msg8]

Consumer needs to read: msg0, msg1, msg2, msg3, msg4, msg5, msg6, msg7, msg8
```

### **Strategy: Sequential Partition Reading**
```java
// Start: partition=0, offset=0
next() → msg0 (partition 0, offset 0) → advance to (0, 1)
next() → msg1 (partition 0, offset 1) → advance to (0, 2)
next() → msg2 (partition 0, offset 2) → advance to (1, 0) // End of partition 0
next() → msg3 (partition 1, offset 0) → advance to (1, 1)
next() → msg4 (partition 1, offset 1) → advance to (2, 0) // End of partition 1
next() → msg5 (partition 2, offset 0) → advance to (2, 1)
// ... and so on
```

### **Position Advancement Logic:**
```java
private void moveToNextMessage() {
    currentOffset++;
    
    // Check if we've reached end of current partition
    Partition partition = topic.getPartition(currentPartition);
    if (currentOffset >= partition.size()) {
        // Move to next partition
        currentPartition++;
        currentOffset = 0;
    }
}

public boolean hasNext() {
    // Are we past the last partition?
    if (currentPartition >= topic.getPartitionCount()) {
        return false;
    }
    
    // Are we at the end of the last partition?
    Partition partition = topic.getPartition(currentPartition);
    return currentOffset < partition.size();
}
```

---

## 🔧 Implementation Strategy

### **Producer Implementation Steps:**
1. **Constructor** - Initialize topic cache and defaults
2. **Input validation** - Check for null/empty topic names and values
3. **Topic management** - Get existing or create new topics
4. **Send method** - Delegate to topic.send()
5. **Utility methods** - Topic existence, listing topics

### **Consumer Implementation Steps:**
1. **Constructor** - Get topic and initialize position
2. **hasNext()** - Check if more messages available
3. **next()** - Read current message and advance position
4. **Position tracking** - Handle partition boundaries correctly
5. **Error handling** - Invalid topics, empty topics

---

## 🚨 Edge Cases and Validation

### **Producer Edge Cases:**
```java
// Input validation
public void send(String topicName, String key, String value) {
    if (topicName == null || topicName.trim().isEmpty()) {
        throw new IllegalArgumentException("Topic name cannot be null or empty");
    }
    if (value == null || value.trim().isEmpty()) {
        throw new IllegalArgumentException("Message value cannot be null or empty");
    }
    // key can be null (round-robin routing)
}

// Topic creation
private Topic getOrCreateTopic(String topicName) {
    return topics.computeIfAbsent(topicName, name -> 
        new Topic(name, defaultPartitionCount)
    );
}
```

### **Consumer Edge Cases:**
```java
// Constructor validation
public Consumer(String topicName) {
    if (topicName == null || topicName.trim().isEmpty()) {
        throw new IllegalArgumentException("Topic name cannot be null or empty");
    }
    // Topic must exist (Producer creates it)
    if (!Producer.topicExists(topicName)) {
        throw new IllegalArgumentException("Topic does not exist: " + topicName);
    }
}

// Empty topic handling
public boolean hasNext() {
    // Handle case where topic has no messages
    if (topic.getTotalMessageCount() == 0) {
        return false;
    }
    // ... rest of logic
}
```

---

## 🎯 Test-Driven Development

### **Producer Tests Guide You:**
1. `testSendMessage` → Basic send functionality
2. `testSendToNewTopic` → Automatic topic creation
3. `testSendMultipleMessages` → Multiple sends work correctly
4. `testInvalidInputs` → Proper validation and error handling

### **Consumer Tests Guide You:**
1. `testReadSingleMessage` → Basic read functionality
2. `testReadMultipleMessages` → Iterator pattern works
3. `testReadFromMultiplePartitions` → Cross-partition reading
4. `testEmptyTopic` → Handle topics with no messages

---

## 🧠 Key Java Concepts You'll Learn

### **1. Map Operations**
```java
// computeIfAbsent for topic caching
topics.computeIfAbsent(topicName, name -> new Topic(name, defaultPartitionCount));

// keySet() for listing topics
public Set<String> getTopicNames() {
    return topics.keySet();
}
```

### **2. Iterator Pattern**
```java
// Classic iterator interface
public boolean hasNext() { ... }
public KafkaMessage next() { ... }

// Usage pattern
while (consumer.hasNext()) {
    KafkaMessage message = consumer.next();
    // process message
}
```

### **3. State Management**
```java
// Consumer maintains reading position
private int currentPartition = 0;
private long currentOffset = 0;

// State changes with each read
private void moveToNextMessage() {
    // Update state based on current position
}
```

---

## 🏭 Real Kafka Insights

### **How Real Kafka Producers Work:**
```java
// Real Kafka Producer (simplified)
Properties props = new Properties();
props.put("bootstrap.servers", "localhost:9092");
props.put("key.serializer", "StringSerializer");
props.put("value.serializer", "StringSerializer");

Producer<String, String> producer = new KafkaProducer<>(props);
producer.send(new ProducerRecord<>("user-events", "user-123", "User logged in"));
```

### **How Real Kafka Consumers Work:**
```java
// Real Kafka Consumer (simplified)
Properties props = new Properties();
props.put("bootstrap.servers", "localhost:9092");
props.put("group.id", "my-consumer-group");
props.put("key.deserializer", "StringDeserializer");
props.put("value.deserializer", "StringDeserializer");

Consumer<String, String> consumer = new KafkaConsumer<>(props);
consumer.subscribe(Arrays.asList("user-events"));

while (true) {
    ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
    for (ConsumerRecord<String, String> record : records) {
        System.out.println("Received: " + record.value());
    }
}
```

---

## 🚀 Ready to Code?

Now you understand:
- ✅ **Why** Producer/Consumer simplify application development
- ✅ **How** they wrap your Topic/Partition infrastructure
- ✅ **What** methods and data structures you need
- ✅ **Which** edge cases to handle

**Next Step**: Look at the tests in `ProducerTest.java` and `ConsumerTest.java` to see exactly what to build!

**Remember**: 
- **Producer = Simple sending** (hide topic management complexity)
- **Consumer = Simple reading** (hide partition/offset complexity)
- **Clean APIs** make your Kafka system easy to use

**Let the tests guide you - they show exactly what behavior to implement!** 🎯
