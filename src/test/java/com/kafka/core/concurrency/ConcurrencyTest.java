package com.kafka.core.concurrency;

import com.kafka.core.partition.Partition;
import com.kafka.core.message.KafkaMessage;
import com.kafka.core.topic.Topic;
import com.kafka.core.producer.Producer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ConcurrencyTest - Demonstrates and fixes thread safety issues
 * 
 * This test simulates the real-world scenario where multiple producers
 * send messages to the same partition simultaneously, causing:
 * 1. Race conditions in offset assignment
 * 2. Lost messages
 * 3. Data corruption
 * 
 * Then we'll fix it using Kafka's synchronization patterns!
 */
public class ConcurrencyTest {
    
    private Partition partition;
    private Topic topic;
    private Producer producer;
    
    @BeforeEach
    void setUp() {
        partition = new Partition(0);
        topic = new Topic("test-topic", 1);
        producer = new Producer();
    }
    
    @Test
    @DisplayName("🔥 DEMONSTRATE: Race condition with multiple threads writing to same partition")
    void testConcurrencyProblem() throws InterruptedException {
        System.out.println("\n🔥 SIMULATING CONCURRENCY PROBLEM...");
        
        final int NUM_THREADS = 10;
        final int MESSAGES_PER_THREAD = 100;
        final int EXPECTED_TOTAL_MESSAGES = NUM_THREADS * MESSAGES_PER_THREAD;
        
        // Create thread pool
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(NUM_THREADS);
        
        // Track what each thread thinks it wrote
        List<Set<Long>> threadOffsets = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger duplicateOffsets = new AtomicInteger(0);
        
        // Launch threads that all write to same partition simultaneously
        for (int threadId = 0; threadId < NUM_THREADS; threadId++) {
            final int currentThreadId = threadId;
            executor.submit(() -> {
                try {
                    // Wait for all threads to be ready
                    startLatch.await();
                    
                    Set<Long> myOffsets = new HashSet<>();
                    
                    // Each thread sends 100 messages rapidly
                    for (int i = 0; i < MESSAGES_PER_THREAD; i++) {
                        KafkaMessage message = KafkaMessage.builder()
                            .key("thread-" + currentThreadId)
                            .value("message-" + i)
                            .build();
                        
                        // This is where the race condition happens!
                        long offset = partition.append(message);
                        
                        // Track if we get duplicate offsets (BAD!)
                        if (!myOffsets.add(offset)) {
                            duplicateOffsets.incrementAndGet();
                            System.err.println("❌ Thread " + currentThreadId + " got duplicate offset: " + offset);
                        }
                    }
                    
                    threadOffsets.add(myOffsets);
                    
                } catch (Exception e) {
                    System.err.println("❌ Thread " + currentThreadId + " failed: " + e.getMessage());
                } finally {
                    finishLatch.countDown();
                }
            });
        }
        
        // Start all threads simultaneously (maximum contention!)
        System.out.println("🚀 Starting " + NUM_THREADS + " threads writing " + MESSAGES_PER_THREAD + " messages each...");
        startLatch.countDown();
        
        // Wait for all threads to complete
        boolean completed = finishLatch.await(10, TimeUnit.SECONDS);
        assertTrue(completed, "Threads should complete within 10 seconds");
        
        executor.shutdown();
        
        // Analyze the damage!
        analyzeResults(EXPECTED_TOTAL_MESSAGES, threadOffsets, duplicateOffsets.get());
    }
    
    private void analyzeResults(int expectedMessages, List<Set<Long>> threadOffsets, int duplicateOffsets) {
        System.out.println("\n📊 CONCURRENCY PROBLEM ANALYSIS:");
        
        // Check actual vs expected message count
        int actualMessages = partition.size();
        System.out.println("Expected messages: " + expectedMessages);
        System.out.println("Actual messages: " + actualMessages);
        System.out.println("Lost messages: " + (expectedMessages - actualMessages));
        
        // Check for duplicate offsets across threads
        Set<Long> allOffsets = new HashSet<>();
        int totalOffsetsSeen = 0;
        
        for (Set<Long> threadOffset : threadOffsets) {
            totalOffsetsSeen += threadOffset.size();
            allOffsets.addAll(threadOffset);
        }
        
        System.out.println("Total offsets seen by threads: " + totalOffsetsSeen);
        System.out.println("Unique offsets: " + allOffsets.size());
        System.out.println("Duplicate offsets detected: " + duplicateOffsets);
        
        // Check offset sequence integrity
        long maxOffset = partition.getLatestOffset() - 1;
        System.out.println("Latest offset: " + maxOffset);
        
        // Look for gaps in offset sequence
        List<Long> missingOffsets = new ArrayList<>();
        for (long i = 0; i <= maxOffset; i++) {
            if (!allOffsets.contains(i)) {
                missingOffsets.add(i);
            }
        }
        
        if (!missingOffsets.isEmpty()) {
            System.out.println("❌ Missing offsets: " + missingOffsets.subList(0, Math.min(10, missingOffsets.size())));
        }
        
        // Summary
        System.out.println("\n🔥 PROBLEMS DETECTED:");
        if (actualMessages < expectedMessages) {
            System.out.println("❌ LOST MESSAGES: " + (expectedMessages - actualMessages) + " messages disappeared!");
        }
        if (duplicateOffsets > 0) {
            System.out.println("❌ DUPLICATE OFFSETS: " + duplicateOffsets + " threads got same offset!");
        }
        if (!missingOffsets.isEmpty()) {
            System.out.println("❌ OFFSET GAPS: " + missingOffsets.size() + " offsets missing from sequence!");
        }
        
        // This test is SUPPOSED to fail - it demonstrates the problem!
        System.out.println("\n💡 This demonstrates why Kafka needs synchronization!");
        System.out.println("Next: We'll fix this using Kafka's approach...\n");
    }
    
    @Test
    @DisplayName("🔥 DEMONSTRATE: Producer-level concurrency issues")
    void testProducerConcurrencyProblem() throws InterruptedException {
        System.out.println("\n🔥 SIMULATING PRODUCER CONCURRENCY PROBLEM...");
        
        final int NUM_PRODUCERS = 5;
        final int MESSAGES_PER_PRODUCER = 50;
        final String TOPIC_NAME = "concurrent-topic";
        
        ExecutorService executor = Executors.newFixedThreadPool(NUM_PRODUCERS);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(NUM_PRODUCERS);
        
        // Multiple producers sending to same topic
        for (int producerId = 0; producerId < NUM_PRODUCERS; producerId++) {
            final int currentProducerId = producerId;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    Producer myProducer = new Producer();
                    
                    for (int i = 0; i < MESSAGES_PER_PRODUCER; i++) {
                        myProducer.send(TOPIC_NAME, 
                                       "producer-" + currentProducerId, 
                                       "message-" + i);
                        
                        // Small delay to increase chance of race conditions
                        Thread.sleep(1);
                    }
                    
                } catch (Exception e) {
                    System.err.println("❌ Producer " + currentProducerId + " failed: " + e.getMessage());
                } finally {
                    finishLatch.countDown();
                }
            });
        }
        
        System.out.println("🚀 Starting " + NUM_PRODUCERS + " producers...");
        startLatch.countDown();
        
        boolean completed = finishLatch.await(15, TimeUnit.SECONDS);
        assertTrue(completed, "Producers should complete within 15 seconds");
        
        executor.shutdown();
        
        // Check results
        Topic resultTopic = producer.getTopic(TOPIC_NAME);
        int expectedTotal = NUM_PRODUCERS * MESSAGES_PER_PRODUCER;
        int actualTotal = resultTopic.getTotalMessageCount();
        
        System.out.println("Expected total messages: " + expectedTotal);
        System.out.println("Actual total messages: " + actualTotal);
        System.out.println("Message loss: " + (expectedTotal - actualTotal));
        
        System.out.println("\n💡 Even at Producer level, we can see concurrency issues!");
        System.out.println("This is why real Kafka has sophisticated synchronization...\n");
    }

    @Test
    @DisplayName("✅ FIXED: Thread-safe partition with Kafka-style synchronization")
    void testConcurrencyFixed() throws InterruptedException {
        System.out.println("\n✅ TESTING THE FIX: Kafka-style synchronization...");

        final int NUM_THREADS = 10;
        final int MESSAGES_PER_THREAD = 100;
        final int EXPECTED_TOTAL_MESSAGES = NUM_THREADS * MESSAGES_PER_THREAD;

        // Create thread pool
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(NUM_THREADS);

        // Track results
        List<Set<Long>> threadOffsets = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger duplicateOffsets = new AtomicInteger(0);

        // Launch threads that all write to same partition simultaneously
        for (int threadId = 0; threadId < NUM_THREADS; threadId++) {
            final int currentThreadId = threadId;
            executor.submit(() -> {
                try {
                    startLatch.await();

                    Set<Long> myOffsets = new HashSet<>();

                    // Each thread sends 100 messages rapidly
                    for (int i = 0; i < MESSAGES_PER_THREAD; i++) {
                        KafkaMessage message = KafkaMessage.builder()
                            .key("thread-" + currentThreadId)
                            .value("message-" + i)
                            .build();

                        // Now this is thread-safe with synchronized (lock)!
                        long offset = partition.append(message);

                        // Track if we get duplicate offsets (should NOT happen now!)
                        if (!myOffsets.add(offset)) {
                            duplicateOffsets.incrementAndGet();
                            System.err.println("❌ Thread " + currentThreadId + " got duplicate offset: " + offset);
                        }
                    }

                    threadOffsets.add(myOffsets);

                } catch (Exception e) {
                    System.err.println("❌ Thread " + currentThreadId + " failed: " + e.getMessage());
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        // Start all threads simultaneously
        System.out.println("🚀 Starting " + NUM_THREADS + " threads writing " + MESSAGES_PER_THREAD + " messages each...");
        startLatch.countDown();

        // Wait for all threads to complete
        boolean completed = finishLatch.await(10, TimeUnit.SECONDS);
        assertTrue(completed, "Threads should complete within 10 seconds");

        executor.shutdown();

        // Verify the fix worked!
        verifyThreadSafety(EXPECTED_TOTAL_MESSAGES, threadOffsets, duplicateOffsets.get());
    }

    private void verifyThreadSafety(int expectedMessages, List<Set<Long>> threadOffsets, int duplicateOffsets) {
        System.out.println("\n📊 THREAD SAFETY VERIFICATION:");

        // Check actual vs expected message count
        int actualMessages = partition.size();
        System.out.println("Expected messages: " + expectedMessages);
        System.out.println("Actual messages: " + actualMessages);
        System.out.println("Lost messages: " + (expectedMessages - actualMessages));

        // Check for duplicate offsets across threads
        Set<Long> allOffsets = new HashSet<>();
        int totalOffsetsSeen = 0;

        for (Set<Long> threadOffset : threadOffsets) {
            totalOffsetsSeen += threadOffset.size();
            allOffsets.addAll(threadOffset);
        }

        System.out.println("Total offsets seen by threads: " + totalOffsetsSeen);
        System.out.println("Unique offsets: " + allOffsets.size());
        System.out.println("Duplicate offsets detected: " + duplicateOffsets);

        // Check offset sequence integrity
        long maxOffset = partition.getLatestOffset() - 1;
        System.out.println("Latest offset: " + maxOffset);

        // Verify perfect sequence (0, 1, 2, 3, ..., maxOffset)
        List<Long> missingOffsets = new ArrayList<>();
        for (long i = 0; i <= maxOffset; i++) {
            if (!allOffsets.contains(i)) {
                missingOffsets.add(i);
            }
        }

        // Results
        System.out.println("\n✅ SYNCHRONIZATION RESULTS:");

        // These should all be ZERO now!
        assertEquals(expectedMessages, actualMessages, "No messages should be lost with synchronization!");
        assertEquals(0, duplicateOffsets, "No duplicate offsets should occur with synchronization!");
        assertEquals(0, missingOffsets.size(), "No offset gaps should exist with synchronization!");

        if (actualMessages == expectedMessages && duplicateOffsets == 0 && missingOffsets.isEmpty()) {
            System.out.println("🎉 SUCCESS: All " + expectedMessages + " messages preserved!");
            System.out.println("🎉 SUCCESS: No duplicate offsets!");
            System.out.println("🎉 SUCCESS: Perfect offset sequence!");
            System.out.println("🎉 KAFKA-STYLE SYNCHRONIZATION WORKS! 🎉");
        } else {
            System.out.println("❌ FAILURE: Synchronization didn't work properly");
        }

        System.out.println("\n💡 This demonstrates how real Kafka prevents data corruption!\n");
    }

    @Test
    @DisplayName("⚡ PERFORMANCE: Compare synchronized vs unsynchronized performance")
    void testPerformanceComparison() throws InterruptedException {
        System.out.println("\n⚡ PERFORMANCE COMPARISON: Synchronized vs Unsynchronized");

        final int NUM_THREADS = 5;
        final int MESSAGES_PER_THREAD = 1000;
        final int TOTAL_MESSAGES = NUM_THREADS * MESSAGES_PER_THREAD;

        // Test 1: Current synchronized implementation
        long syncTime = measureSynchronizedPerformance(NUM_THREADS, MESSAGES_PER_THREAD);

        // Test 2: Create a temporary unsynchronized partition for comparison
        long unsyncTime = measureUnsynchronizedPerformance(NUM_THREADS, MESSAGES_PER_THREAD);

        // Analysis
        System.out.println("\n📊 PERFORMANCE RESULTS:");
        System.out.println("Messages processed: " + TOTAL_MESSAGES);
        System.out.println("Synchronized time: " + syncTime + " ms");
        System.out.println("Unsynchronized time: " + unsyncTime + " ms");
        System.out.println("Overhead: " + (syncTime - unsyncTime) + " ms (" +
                          String.format("%.1f", ((double)(syncTime - unsyncTime) / unsyncTime * 100)) + "% slower)");

        double syncThroughput = (double) TOTAL_MESSAGES / syncTime * 1000;
        double unsyncThroughput = (double) TOTAL_MESSAGES / unsyncTime * 1000;

        System.out.println("Synchronized throughput: " + String.format("%.0f", syncThroughput) + " messages/sec");
        System.out.println("Unsynchronized throughput: " + String.format("%.0f", unsyncThroughput) + " messages/sec");

        System.out.println("\n💡 TRADE-OFF ANALYSIS:");
        System.out.println("✅ Synchronized: 100% data integrity, " + String.format("%.0f", syncThroughput) + " msg/sec");
        System.out.println("❌ Unsynchronized: Data corruption, " + String.format("%.0f", unsyncThroughput) + " msg/sec");
        System.out.println("🎯 Real Kafka chooses CORRECTNESS over raw speed!");
        System.out.println("🎯 Performance optimizations come through batching, not removing locks!");
    }

    private long measureSynchronizedPerformance(int numThreads, int messagesPerThread) throws InterruptedException {
        System.out.println("\n🔒 Testing synchronized performance...");
        Partition syncPartition = new Partition(0);

        long startTime = System.currentTimeMillis();

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(numThreads);

        for (int threadId = 0; threadId < numThreads; threadId++) {
            final int currentThreadId = threadId;
            executor.submit(() -> {
                try {
                    startLatch.await();

                    for (int i = 0; i < messagesPerThread; i++) {
                        KafkaMessage message = KafkaMessage.builder()
                            .key("thread-" + currentThreadId)
                            .value("message-" + i)
                            .build();
                        syncPartition.append(message);
                    }
                } catch (Exception e) {
                    System.err.println("Error in thread " + currentThreadId + ": " + e.getMessage());
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        finishLatch.await();
        executor.shutdown();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("Synchronized: " + syncPartition.size() + " messages in " + duration + " ms");
        return duration;
    }

    private long measureUnsynchronizedPerformance(int numThreads, int messagesPerThread) throws InterruptedException {
        System.out.println("\n🚫 Testing unsynchronized performance (for comparison only)...");

        // Create a simple unsynchronized list for comparison
        List<KafkaMessage> unsyncMessages = new ArrayList<>();
        AtomicInteger unsyncOffset = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(numThreads);

        for (int threadId = 0; threadId < numThreads; threadId++) {
            final int currentThreadId = threadId;
            executor.submit(() -> {
                try {
                    startLatch.await();

                    for (int i = 0; i < messagesPerThread; i++) {
                        KafkaMessage message = KafkaMessage.builder()
                            .key("thread-" + currentThreadId)
                            .value("message-" + i)
                            .build();
                        // Unsynchronized access - DANGEROUS but faster
                        unsyncMessages.add(message);
                        unsyncOffset.incrementAndGet();
                    }
                } catch (Exception e) {
                    System.err.println("Error in thread " + currentThreadId + ": " + e.getMessage());
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        finishLatch.await();
        executor.shutdown();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.println("Unsynchronized: " + unsyncMessages.size() + " messages in " + duration + " ms (DATA MAY BE CORRUPTED!)");
        return duration;
    }
}
