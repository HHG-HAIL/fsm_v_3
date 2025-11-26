package com.fsm.notification.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Notification entity.
 */
class NotificationTest {
    
    @Test
    void testNotificationBuilder() {
        LocalDateTime now = LocalDateTime.now();
        
        Notification notification = Notification.builder()
            .id(1L)
            .userId(101L)
            .type(Notification.NotificationType.PUSH)
            .title("Test Notification")
            .message("Test message")
            .data("{\"key\": \"value\"}")
            .read(false)
            .sentAt(now)
            .deliveredAt(now)
            .createdAt(now)
            .build();
        
        assertNotNull(notification);
        assertEquals(1L, notification.getId());
        assertEquals(101L, notification.getUserId());
        assertEquals(Notification.NotificationType.PUSH, notification.getType());
        assertEquals("Test Notification", notification.getTitle());
        assertEquals("Test message", notification.getMessage());
        assertEquals("{\"key\": \"value\"}", notification.getData());
        assertFalse(notification.getRead());
        assertEquals(now, notification.getSentAt());
        assertEquals(now, notification.getDeliveredAt());
        assertEquals(now, notification.getCreatedAt());
    }
    
    @Test
    void testNotificationDefaultReadStatus() {
        Notification notification = Notification.builder()
            .userId(101L)
            .type(Notification.NotificationType.PUSH)
            .title("Test")
            .message("Test message")
            .build();
        
        assertFalse(notification.getRead());
    }
    
    @Test
    void testMarkAsRead() {
        Notification notification = Notification.builder()
            .userId(101L)
            .type(Notification.NotificationType.PUSH)
            .title("Test")
            .message("Test message")
            .read(false)
            .build();
        
        assertFalse(notification.getRead());
        
        notification.markAsRead();
        
        assertTrue(notification.getRead());
    }
    
    @Test
    void testMarkAsSent() {
        Notification notification = Notification.builder()
            .userId(101L)
            .type(Notification.NotificationType.PUSH)
            .title("Test")
            .message("Test message")
            .build();
        
        assertNull(notification.getSentAt());
        
        notification.markAsSent();
        
        assertNotNull(notification.getSentAt());
        assertTrue(notification.getSentAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }
    
    @Test
    void testMarkAsDelivered() {
        Notification notification = Notification.builder()
            .userId(101L)
            .type(Notification.NotificationType.PUSH)
            .title("Test")
            .message("Test message")
            .build();
        
        assertNull(notification.getDeliveredAt());
        
        notification.markAsDelivered();
        
        assertNotNull(notification.getDeliveredAt());
        assertTrue(notification.getDeliveredAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }
    
    @Test
    void testIsSent() {
        Notification notification = Notification.builder()
            .userId(101L)
            .type(Notification.NotificationType.PUSH)
            .title("Test")
            .message("Test message")
            .build();
        
        assertFalse(notification.isSent());
        
        notification.markAsSent();
        
        assertTrue(notification.isSent());
    }
    
    @Test
    void testIsDelivered() {
        Notification notification = Notification.builder()
            .userId(101L)
            .type(Notification.NotificationType.PUSH)
            .title("Test")
            .message("Test message")
            .build();
        
        assertFalse(notification.isDelivered());
        
        notification.markAsDelivered();
        
        assertTrue(notification.isDelivered());
    }
    
    @Test
    void testIsRead() {
        Notification notification = Notification.builder()
            .userId(101L)
            .type(Notification.NotificationType.PUSH)
            .title("Test")
            .message("Test message")
            .read(false)
            .build();
        
        assertFalse(notification.isRead());
        
        notification.markAsRead();
        
        assertTrue(notification.isRead());
    }
    
    @Test
    void testIsReadWithNullValue() {
        Notification notification = new Notification();
        notification.setRead(null);
        
        assertFalse(notification.isRead());
    }
    
    @Test
    void testNotificationTypeEnum() {
        assertEquals(3, Notification.NotificationType.values().length);
        assertNotNull(Notification.NotificationType.valueOf("PUSH"));
        assertNotNull(Notification.NotificationType.valueOf("EMAIL"));
        assertNotNull(Notification.NotificationType.valueOf("SMS"));
    }
    
    @Test
    void testPrePersistSetsCreatedAt() {
        Notification notification = Notification.builder()
            .userId(101L)
            .type(Notification.NotificationType.PUSH)
            .title("Test")
            .message("Test message")
            .build();
        
        assertNull(notification.getCreatedAt());
        
        notification.onCreate();
        
        assertNotNull(notification.getCreatedAt());
        assertTrue(notification.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }
    
    @Test
    void testPrePersistSetsDefaultReadStatus() {
        Notification notification = new Notification();
        notification.setUserId(101L);
        notification.setType(Notification.NotificationType.PUSH);
        notification.setTitle("Test");
        notification.setMessage("Test message");
        
        notification.onCreate();
        
        assertNotNull(notification.getRead());
        assertFalse(notification.getRead());
    }
    
    @Test
    void testNoArgsConstructor() {
        Notification notification = new Notification();
        
        assertNotNull(notification);
        assertNull(notification.getId());
        assertNull(notification.getUserId());
        assertNull(notification.getType());
        assertNull(notification.getTitle());
        assertNull(notification.getMessage());
        assertNull(notification.getData());
        // @Builder.Default applies to all constructors in Lombok
        assertFalse(notification.getRead());
        assertNull(notification.getSentAt());
        assertNull(notification.getDeliveredAt());
        assertNull(notification.getCreatedAt());
    }
    
    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        
        Notification notification = new Notification(
            1L, 101L, Notification.NotificationType.EMAIL, 
            "Title", "Message", "{}", false, now, now, now
        );
        
        assertNotNull(notification);
        assertEquals(1L, notification.getId());
        assertEquals(101L, notification.getUserId());
        assertEquals(Notification.NotificationType.EMAIL, notification.getType());
        assertEquals("Title", notification.getTitle());
        assertEquals("Message", notification.getMessage());
        assertEquals("{}", notification.getData());
        assertFalse(notification.getRead());
        assertEquals(now, notification.getSentAt());
        assertEquals(now, notification.getDeliveredAt());
        assertEquals(now, notification.getCreatedAt());
    }
    
    @Test
    void testSettersAndGetters() {
        Notification notification = new Notification();
        LocalDateTime now = LocalDateTime.now();
        
        notification.setId(1L);
        notification.setUserId(101L);
        notification.setType(Notification.NotificationType.SMS);
        notification.setTitle("Title");
        notification.setMessage("Message");
        notification.setData("{\"test\": true}");
        notification.setRead(true);
        notification.setSentAt(now);
        notification.setDeliveredAt(now);
        notification.setCreatedAt(now);
        
        assertEquals(1L, notification.getId());
        assertEquals(101L, notification.getUserId());
        assertEquals(Notification.NotificationType.SMS, notification.getType());
        assertEquals("Title", notification.getTitle());
        assertEquals("Message", notification.getMessage());
        assertEquals("{\"test\": true}", notification.getData());
        assertTrue(notification.getRead());
        assertEquals(now, notification.getSentAt());
        assertEquals(now, notification.getDeliveredAt());
        assertEquals(now, notification.getCreatedAt());
    }
}
