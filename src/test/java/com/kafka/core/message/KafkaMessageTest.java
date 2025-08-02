package com.kafka.core.message;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Set;
import java.util.HashSet;

/**
 * Simple tests for KafkaMessage - starting with what you know!
 * 
 * We'll test:
 * 1. Creating messages with key and value
 * 2. What happens with null keys (important for partitioning!)
 * 3. Basic functionality
 * 
 * As we learn more Kafka concepts, we'll add more tests.
 */
public class KafkaMessageTest {
    
    @Test
    @DisplayName("Should create message with key and value")
    void testCreateMessageWithKeyAndValue() {
        // Given: I want to send a message to a topic
        String key = "user-123";           // This determines which partition
        String value = "Hello, Kafka!";    // This is the actual message
        
        // When: I create the message
        KafkaMessage message = KafkaMessage.builder()
            .key(key)
            .value(value)
            .build();
        
        // Then: The message should contain my data
        assertEquals(key, message.getKey());
        assertEquals(value, message.getValue());
    }
    
    @Test
    @DisplayName("Should create message with only value (no key)")
    void testCreateMessageWithoutKey() {
        // Given: I want to send a message but don't care about partitioning
        String value = "Hello, World!";
        
        // When: I create message without a key
        KafkaMessage message = KafkaMessage.builder()
            .value(value)
            .build();
        
        // Then: Key should be null, value should be set
        assertNull(message.getKey());      // No key = random partition assignment
        assertEquals(value, message.getValue());
    }
    
    @Test
    @DisplayName("Should create message with null key explicitly")
    void testCreateMessageWithNullKey() {
        // Given: I explicitly set key to null
        String value = "Another message";
        
        // When: I create message with explicit null key
        KafkaMessage message = KafkaMessage.builder()
            .key(null)                     // Explicitly null
            .value(value)
            .build();
        
        // Then: Should work the same as no key
        assertNull(message.getKey());
        assertEquals(value, message.getValue());
    }
    
    @Test
    @DisplayName("Should have readable toString for debugging")
    void testToString() {
        // Given: A message with key and value
        KafkaMessage message = KafkaMessage.builder()
            .key("test-key")
            .value("test-value")
            .build();
        
        // When: I convert to string
        String result = message.toString();
        
        // Then: Should be readable and contain both key and value
        assertTrue(result.contains("test-key"));
        assertTrue(result.contains("test-value"));
        assertTrue(result.contains("KafkaMessage"));
    }
    
    @Test
    @DisplayName("Should validate that value is not null")
    void testValidateValueNotNull() {
        // Given: I try to create a message with null value
        // When/Then: Should throw exception
        assertThrows(IllegalArgumentException.class, () -> {
            KafkaMessage.builder()
                .key("valid-key")
                .value(null)  // This should be rejected
                .build();
        });
    }

    @Test
    @DisplayName("Should validate that value is not empty")
    void testValidateValueNotEmpty() {
        // Given: I try to create a message with empty value
        // When/Then: Should this be allowed? Let's say no for now
        assertThrows(IllegalArgumentException.class, () -> {
            KafkaMessage.builder()
                .key("valid-key")
                .value("")  // Empty string - should this be allowed?
                .build();
        });
    }

    @Test
    @DisplayName("Should implement equals() correctly")
    void testEqualsMethod() {
        // Given: Two messages with same content
        KafkaMessage message1 = KafkaMessage.builder()
            .key("user-123")
            .value("Hello World")
            .build();

        KafkaMessage message2 = KafkaMessage.builder()
            .key("user-123")
            .value("Hello World")
            .build();

        KafkaMessage differentMessage = KafkaMessage.builder()
            .key("user-456")
            .value("Hello World")
            .build();

        // When/Then: Should be equal if content is same
        assertEquals(message1, message2);
        assertNotEquals(message1, differentMessage);
        assertNotEquals(message1, null);
        assertNotEquals(message1, "not a message");
    }

    @Test
    @DisplayName("Should implement hashCode() correctly")
    void testHashCodeMethod() {
        // Given: Two messages with same content
        KafkaMessage message1 = KafkaMessage.builder()
            .key("user-123")
            .value("Hello World")
            .build();

        KafkaMessage message2 = KafkaMessage.builder()
            .key("user-123")
            .value("Hello World")
            .build();

        // When/Then: Should have same hash code if equal
        assertEquals(message1.hashCode(), message2.hashCode());
    }

    @Test
    @DisplayName("Should handle null keys in equals() method")
    void testEqualsWithNullKeys() {
        // Given: Two messages with null keys
        KafkaMessage message1 = KafkaMessage.builder()
            .value("Test message")
            .build();

        KafkaMessage message2 = KafkaMessage.builder()
            .value("Test message")
            .build();

        // When/Then: Should be equal without throwing NullPointerException
        assertEquals(message1, message2);
        assertEquals(message1.hashCode(), message2.hashCode());
    }

    @Test
    @DisplayName("Should work correctly in HashSet (equals/hashCode contract)")
    void testHashSetBehavior() {
        // Given: A HashSet and two equal messages
        Set<KafkaMessage> messageSet = new HashSet<>();

        KafkaMessage message1 = KafkaMessage.builder()
            .key("user-123")
            .value("Hello World")
            .build();

        KafkaMessage message2 = KafkaMessage.builder()
            .key("user-123")
            .value("Hello World")
            .build();

        // When: I add both messages to set
        messageSet.add(message1);
        messageSet.add(message2);  // Should not add duplicate

        // Then: Set should contain only one message
        assertEquals(1, messageSet.size());
        assertTrue(messageSet.contains(message1));
        assertTrue(messageSet.contains(message2));
    }

    // Questions to think about while implementing:
    // 1. What happens if value is null? Should we allow it?
    // 2. How does partitioning work when key is null?
    // 3. Why are equals() and hashCode() important for data structures?
    // 4. What should be included in equals comparison? Key? Value? Both?
    // 5. If two objects are equal, must their hashCode be the same?
}
