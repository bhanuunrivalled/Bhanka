# Kafka Internals - Data Structures Learning

Learn Java server-side programming and data structures by implementing Kafka's core components in memory.

## 🚀 Quick Start

1. **Run tests to see what needs to be implemented**:
   ```bash
   mvn test
   ```

2. **Follow the learning path**: See `KAFKA_LEARNING_PATH.md` for complete guidance

3. **Current task**: Implement Partition Constructor and Basic Methods
   ```bash
   mvn test -Dtest=PartitionTest
   ```

## 📋 Current Progress

- ✅ **Phase 1: KafkaMessage** - COMPLETE (10/10 tests passing)
- 🔄 **Phase 2: Partition** - IN PROGRESS (0/7 tests passing)
- ⏳ **Phase 3: Topic** - NOT STARTED
- ⏳ **Phase 4: Producer/Consumer** - NOT STARTED

## 📚 Learning Approach

**Test-Driven Development (TDD)**:
- Tests are fully implemented (guide your work)
- Implementation classes have TODOs and guidance
- You implement code to make tests pass
- Incremental learning, one concept at a time

## 📁 Key Files

- **`KAFKA_LEARNING_PATH.md`** - Complete learning guide (single source of truth)
- **`src/test/java/`** - Complete tests (define what to build)
- **`src/main/java/`** - Your implementations (with TODOs)
- **`pom.xml`** - Maven build configuration

## 🎯 Next Steps

**Current Task**: Implement Partition Constructor and Basic Methods

1. Open `src/main/java/com/kafka/core/partition/Partition.java`
2. Read the TODOs and guidance comments
3. Implement constructor and basic methods
4. Run `mvn test -Dtest=PartitionTest` to see progress

## 🛠️ Technologies

- Java 17, Maven, JUnit 5
- KISS Principle (Keep It Simple, Stupid)

**For detailed guidance, see `KAFKA_LEARNING_PATH.md`** 📖
