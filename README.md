# Kafka Internals - Data Structures Learning

Learn Java server-side programming and data structures by implementing Kafka's core components in memory.

## 🚀 Quick Start

1. **Run tests to see what needs to be implemented**:
   ```bash
   mvn test
   ```

2. **Follow the learning path**: See `KAFKA_LEARNING_PATH.md` for complete guidance

3. **All phases complete!** Run the full test suite:
   ```bash
   mvn test  # All 39 tests passing!
   ```

## 📋 Current Progress

- ✅ **Phase 1: KafkaMessage** - COMPLETE (10/10 tests passing)
- ✅ **Phase 2: Partition** - COMPLETE (7/7 tests passing)
- ✅ **Phase 3: Topic** - COMPLETE (5/5 tests passing)
- ✅ **Phase 4: Producer/Consumer** - COMPLETE (13/13 tests passing)
- ✅ **Phase 6: Concurrency & Thread Safety** - COMPLETE (4/4 tests passing)

**🎯 Total: 39/39 tests passing!**

## 📚 Learning Approach

**Test-Driven Development (TDD)**:
- Tests are fully implemented (guide your work)
- Implementation classes have TODOs and guidance
- You implement code to make tests pass
- Incremental learning, one concept at a time

## 📁 Key Files

- **`KAFKA_LEARNING_PATH.md`** - Complete learning guide (single source of truth)
- **`lessons/`** - Concept explanations (read these first!)
- **`src/test/java/`** - Complete tests (define what to build)
- **`src/main/java/`** - Your implementations (with TODOs)
- **`pom.xml`** - Maven build configuration

## 🎯 Next Steps

## 🏗️ **System Architecture**

```mermaid
graph TB
    subgraph KafkaSystem["Kafka System"]
        TR[TopicRegistry<br/>Centralized Broker]

        subgraph Topics
            T1[Topic: user-events<br/>3 partitions]
            T2[Topic: order-events<br/>2 partitions]
            T3[Topic: logs<br/>1 partition]
        end

        TR --> T1
        TR --> T2
        TR --> T3

        subgraph Partitions["Partitions (user-events)"]
            P1[Partition 0<br/>Messages: 0,1,2...]
            P2[Partition 1<br/>Messages: 0,1,2...]
            P3[Partition 2<br/>Messages: 0,1,2...]
        end

        T1 --> P1
        T1 --> P2
        T1 --> P3
    end

    subgraph Clients
        PROD[Producer<br/>Sends messages]
        CONS[Consumer<br/>Reads messages]
    end

    PROD -.->|connects to| TR
    CONS -.->|connects to| TR

    PROD -->|send message| T1
    T1 -->|hash routing| P1
    T1 -->|hash routing| P2
    T1 -->|hash routing| P3

    P1 -->|read sequentially| CONS
    P2 -->|read sequentially| CONS
    P3 -->|read sequentially| CONS

    style TR fill:#e1f5fe
    style PROD fill:#f3e5f5
    style CONS fill:#e8f5e8
    style T1 fill:#fff3e0
    style P1 fill:#fce4ec
    style P2 fill:#fce4ec
    style P3 fill:#fce4ec
```

## 🎯 **What You've Built - Complete Kafka MVP!**

**Core Components:**
- ✅ **KafkaMessage** - Immutable message objects with builder pattern
- ✅ **Partition** - Append-only log with sequential offsets (Thread-Safe!)
- ✅ **Topic** - Hash-based message routing across multiple partitions
- ✅ **Producer** - Clean API for sending messages to topics
- ✅ **Consumer** - Iterator-style API for reading messages
- ✅ **TopicRegistry** - Centralized broker for topic management

**Key Features:**
- 🔀 **Hash-based partitioning** for consistent message routing
- 📝 **Sequential offsets** for message ordering within partitions
- 🔄 **Round-robin distribution** for null-key messages
- 🏗️ **Clean architecture** with independent Producer/Consumer
- 🔒 **Thread safety** using real Kafka synchronization patterns
- ⚡ **Performance analysis** - 625K+ messages/sec with data integrity
- ✅ **Comprehensive testing** with 39 passing tests

## 🛠️ Technologies

- Java 17, Maven, JUnit 5
- KISS Principle (Keep It Simple, Stupid)

**For detailed guidance, see `KAFKA_LEARNING_PATH.md`** 📖
