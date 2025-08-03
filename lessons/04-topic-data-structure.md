# Lesson 4: Topic Data Structure Deep Dive

## 🎯 What Exactly Is a Topic?

A **Topic** is like a **smart filing cabinet** where:
- Each **drawer** is a **partition** (we already built these!)
- The **filing system** knows which **drawer** to put each **document** (message)
- **Related documents** always go to the **same drawer** (same key = same partition)
- **Random documents** get **evenly distributed** across drawers

---

## 🏗️ Topic Architecture

### **Topic Contains Multiple Partitions:**
```
📚 Topic: "user-events" (3 partitions)
┌─────────────────────────────────────────────────┐
│                    TOPIC                        │
├─────────────────┬─────────────────┬─────────────┤
│   Partition 0   │   Partition 1   │ Partition 2 │
├─────────────────┼─────────────────┼─────────────┤
│ [msg0][msg3]... │ [msg1][msg4]... │ [msg2][msg5]│
│ [msg6][msg9]... │ [msg7][msg10].. │ [msg8][msg11│
└─────────────────┴─────────────────┴─────────────┘
```

### **Message Routing Logic:**
```java
// For messages WITH keys:
int partition = Math.abs(key.hashCode()) % partitionCount;

// For messages WITHOUT keys (null):
int partition = roundRobinCounter++ % partitionCount;
```

---

## 🔧 Implementation Strategy

### **Core Data Structure:**
```java
public class Topic {
    private final String name;                    // Topic identifier
    private final List<Partition> partitions;     // Our partition collection
    private int roundRobinCounter = 0;            // For null key distribution
    
    // Constructor
    public Topic(String name, int partitionCount) {
        this.name = name;
        this.partitions = new ArrayList<>();
        
        // Create the specified number of partitions
        for (int i = 0; i < partitionCount; i++) {
            partitions.add(new Partition(i));
        }
    }
}
```

### **Why This Design?**
- ✅ **Encapsulation**: Topic manages its own partitions
- ✅ **Scalability**: Easy to add more partitions
- ✅ **Abstraction**: Clients don't need to know about partitions
- ✅ **Consistency**: Routing logic centralized in one place

---

## 🎯 Methods We Need to Implement

### **1. Constructors**
```java
// Default constructor (1 partition)
public Topic(String name) {
    this(name, 1);  // Delegate to main constructor
}

// Main constructor (specified partition count)
public Topic(String name, int partitionCount) {
    // TODO: Validate inputs
    // TODO: Create partitions
}
```

### **2. Message Routing - The Heart of Topic**
```java
public void send(String key, String value) {
    // Create the message
    KafkaMessage message = KafkaMessage.builder()
        .key(key)
        .value(value)
        .build();
    
    // Determine which partition to use
    int partitionIndex = calculatePartition(key);
    
    // Send to that partition
    partitions.get(partitionIndex).append(message);
}

private int calculatePartition(String key) {
    if (key == null) {
        // Round-robin for null keys
        return roundRobinCounter++ % partitions.size();
    } else {
        // Hash-based for non-null keys
        return Math.abs(key.hashCode()) % partitions.size();
    }
}
```

### **3. Partition Access**
```java
public Partition getPartition(int index) {
    // TODO: Validate index bounds
    return partitions.get(index);
}

public int getPartitionCount() {
    return partitions.size();
}
```

### **4. Metadata and Statistics**
```java
public String getName() {
    return name;
}

public int getTotalMessageCount() {
    // Sum messages across all partitions
    return partitions.stream()
        .mapToInt(Partition::size)
        .sum();
}
```

---

## 🧮 Routing Algorithm Examples

### **Hash-Based Routing (With Keys):**
```java
// Topic with 3 partitions
Topic topic = new Topic("user-events", 3);

// Examples of key routing:
"user-123".hashCode() = 123456789
Math.abs(123456789) % 3 = 0  → Partition 0

"user-456".hashCode() = 987654321  
Math.abs(987654321) % 3 = 0  → Partition 0 (collision!)

"user-789".hashCode() = 555666777
Math.abs(555666777) % 3 = 1  → Partition 1

"admin-001".hashCode() = 111222333
Math.abs(111222333) % 3 = 2  → Partition 2
```

### **Round-Robin Routing (Null Keys):**
```java
// Starting with roundRobinCounter = 0
topic.send(null, "Anonymous message 1");  // → Partition 0, counter = 1
topic.send(null, "Anonymous message 2");  // → Partition 1, counter = 2  
topic.send(null, "Anonymous message 3");  // → Partition 2, counter = 3
topic.send(null, "Anonymous message 4");  // → Partition 0, counter = 4 (wraps around)
```

---

## 🚨 Edge Cases and Validation

### **Input Validation:**
```java
public Topic(String name, int partitionCount) {
    if (name == null || name.trim().isEmpty()) {
        throw new IllegalArgumentException("Topic name cannot be null or empty");
    }
    if (partitionCount <= 0) {
        throw new IllegalArgumentException("Partition count must be positive");
    }
    // ... rest of constructor
}
```

### **Partition Index Bounds:**
```java
public Partition getPartition(int index) {
    if (index < 0 || index >= partitions.size()) {
        throw new IllegalArgumentException("Partition index %d is out of bounds".formatted(index));
    }
    return partitions.get(index);
}
```

### **Negative Hash Codes:**
```java
private int calculatePartition(String key) {
    if (key == null) {
        return roundRobinCounter++ % partitions.size();
    } else {
        // Handle negative hash codes properly
        return Math.abs(key.hashCode()) % partitions.size();
    }
}
```

---

## 🎯 Test-Driven Development

### **The Tests Tell You What to Build:**
1. `testCreateTopicWithName` → Basic constructor with default partitions
2. `testCreateTopicWithPartitions` → Constructor with specified partition count
3. `testSendMessageToPartition` → Hash-based routing for keyed messages
4. `testSendMessageWithNullKey` → Round-robin for null keys
5. `testMessageOrderingWithinPartition` → Verify ordering guarantees

### **TDD Workflow for Topics:**
1. **Red**: Run test → See it fail (Topic class doesn't exist)
2. **Green**: Write minimal code → Make it pass
3. **Refactor**: Improve code → Keep tests passing
4. **Repeat**: Move to next test

---

## 🧠 Key Java Concepts You'll Learn

### **1. Collection Management**
- ArrayList of Partition objects
- Stream operations for aggregation
- Index-based access and bounds checking

### **2. Hash Functions**
- Understanding hashCode() behavior
- Handling negative hash codes with Math.abs()
- Modulo arithmetic for distribution

### **3. Object Composition**
- Topic "has-a" List of Partitions
- Delegation pattern (Topic delegates to Partition)
- Encapsulation of complex logic

### **4. Method Overloading**
- Multiple constructors with different parameters
- Default parameter simulation with delegation

---

## 🔍 Advanced Concepts

### **Why Round-Robin for Null Keys?**
```java
// Round-robin ensures even distribution
Partition 0: 33.33% of null-key messages
Partition 1: 33.33% of null-key messages  
Partition 2: 33.33% of null-key messages

// Random would be uneven over time
Partition 0: ~30-40% of messages (unpredictable)
Partition 1: ~25-35% of messages (unpredictable)
Partition 2: ~30-40% of messages (unpredictable)
```

### **Hash Collision Handling:**
```java
// Different keys can hash to same partition (this is OK!)
"user-123" → Partition 0
"admin-456" → Partition 0  // Same partition, different keys

// Messages still maintain order within partition
Partition 0: [user-123: "login"][admin-456: "create"][user-123: "logout"]
```

### **Partition Count Considerations:**
```java
// More partitions = better parallelism, but...
Topic topic1 = new Topic("events", 1);   // 1 consumer max
Topic topic2 = new Topic("events", 10);  // 10 consumers max
Topic topic3 = new Topic("events", 100); // 100 consumers max (overkill?)

// Sweet spot: Number of partitions ≈ Number of expected consumers
```

---

## 🚀 Ready to Code?

Now you understand:
- ✅ **Why** topics organize partitions (logical grouping)
- ✅ **How** message routing works (hash-based + round-robin)
- ✅ **What** data structures to use (ArrayList of Partitions)
- ✅ **Which** edge cases to handle (validation, negative hashes)

**Next Step**: Open `src/main/java/com/kafka/core/topic/Topic.java` and start implementing!

**Remember**: 
- **Topic = Smart Router** that sends messages to the right partition
- **Same key = Same partition = Ordered messages**
- **Null key = Round-robin = Even distribution**

**Let the tests guide you - they show exactly what to build!** 🎯
