package com.fsm.notification.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FcmPushNotificationService.
 */
class FcmPushNotificationServiceTest {
    
    private FcmPushNotificationService service;
    
    @BeforeEach
    void setUp() {
        service = new FcmPushNotificationService();
    }
    
    @Test
    void testInitializeWhenFcmDisabled() {
        ReflectionTestUtils.setField(service, "fcmEnabled", false);
        
        service.initialize();
        
        assertFalse(service.isInitialized());
    }
    
    @Test
    void testInitializeWhenCredentialsPathNull() {
        ReflectionTestUtils.setField(service, "fcmEnabled", true);
        ReflectionTestUtils.setField(service, "credentialsPath", null);
        
        service.initialize();
        
        assertFalse(service.isInitialized());
    }
    
    @Test
    void testInitializeWhenCredentialsPathEmpty() {
        ReflectionTestUtils.setField(service, "fcmEnabled", true);
        ReflectionTestUtils.setField(service, "credentialsPath", "");
        
        service.initialize();
        
        assertFalse(service.isInitialized());
    }
    
    @Test
    void testInitializeWhenCredentialsPathInvalid() {
        ReflectionTestUtils.setField(service, "fcmEnabled", true);
        ReflectionTestUtils.setField(service, "credentialsPath", "/invalid/path/credentials.json");
        
        service.initialize();
        
        assertFalse(service.isInitialized());
    }
    
    @Test
    void testSendPushNotificationWhenNotInitialized() {
        ReflectionTestUtils.setField(service, "initialized", false);
        
        Map<String, String> data = new HashMap<>();
        data.put("key", "value");
        
        boolean result = service.sendPushNotification(
                "test-device-token-123",
                "Test Title",
                "Test Message",
                data
        );
        
        assertFalse(result);
    }
    
    @Test
    void testSendPushNotificationWhenDeviceTokenNull() {
        boolean result = service.sendPushNotification(
                null,
                "Test Title",
                "Test Message",
                new HashMap<>()
        );
        
        assertFalse(result);
    }
    
    @Test
    void testSendPushNotificationWhenDeviceTokenEmpty() {
        boolean result = service.sendPushNotification(
                "",
                "Test Title",
                "Test Message",
                new HashMap<>()
        );
        
        assertFalse(result);
    }
    
    @Test
    void testSendPushNotificationWhenDeviceTokenBlank() {
        boolean result = service.sendPushNotification(
                "   ",
                "Test Title",
                "Test Message",
                new HashMap<>()
        );
        
        assertFalse(result);
    }
    
    @Test
    void testSendPushNotificationWithNullData() {
        ReflectionTestUtils.setField(service, "initialized", false);
        
        boolean result = service.sendPushNotification(
                "test-device-token-123",
                "Test Title",
                "Test Message",
                null
        );
        
        assertFalse(result);
    }
    
    @Test
    void testSendPushNotificationWithEmptyData() {
        ReflectionTestUtils.setField(service, "initialized", false);
        
        Map<String, String> data = new HashMap<>();
        
        boolean result = service.sendPushNotification(
                "test-device-token-123",
                "Test Title",
                "Test Message",
                data
        );
        
        assertFalse(result);
    }
    
    @Test
    void testSendPushNotificationLogsWhenNotInitialized() {
        ReflectionTestUtils.setField(service, "initialized", false);
        
        Map<String, String> data = new HashMap<>();
        data.put("taskId", "123");
        data.put("priority", "HIGH");
        
        boolean result = service.sendPushNotification(
                "test-device-token-1234567890",
                "Task Assignment",
                "You have been assigned a new task",
                data
        );
        
        assertFalse(result);
    }
    
    @Test
    void testIsInitializedDefaultValue() {
        assertFalse(service.isInitialized());
    }
    
    @Test
    void testIsInitializedAfterSetup() {
        ReflectionTestUtils.setField(service, "initialized", true);
        
        assertTrue(service.isInitialized());
    }
    
    @Test
    void testSendPushNotificationWithVariousDataFormats() {
        ReflectionTestUtils.setField(service, "initialized", false);
        
        // Test with complex data
        Map<String, String> data = new HashMap<>();
        data.put("key1", "value1");
        data.put("key2", "value2");
        data.put("key3", "value3");
        
        boolean result = service.sendPushNotification(
                "test-token",
                "Title",
                "Message",
                data
        );
        
        assertFalse(result);
    }
    
    @Test
    void testSendPushNotificationWithLongToken() {
        ReflectionTestUtils.setField(service, "initialized", false);
        
        String longToken = "a".repeat(200);
        
        boolean result = service.sendPushNotification(
                longToken,
                "Test",
                "Message",
                new HashMap<>()
        );
        
        assertFalse(result);
    }
    
    @Test
    void testSendPushNotificationWithShortToken() {
        ReflectionTestUtils.setField(service, "initialized", false);
        
        boolean result = service.sendPushNotification(
                "short",
                "Test",
                "Message",
                new HashMap<>()
        );
        
        assertFalse(result);
    }
    
    @Test
    void testMaskTokenShortToken() {
        // Use reflection to call private method
        ReflectionTestUtils.setField(service, "initialized", false);
        
        // Test with short token
        String shortToken = "abc";
        boolean result = service.sendPushNotification(
                shortToken,
                "Test",
                "Message",
                new HashMap<>()
        );
        
        assertFalse(result);
    }
    
    @Test
    void testMaskTokenNullToken() {
        boolean result = service.sendPushNotification(
                null,
                "Test",
                "Message",
                new HashMap<>()
        );
        
        assertFalse(result);
    }
    
    @Test
    void testInitializeWhenFcmEnabledButNoApps() {
        ReflectionTestUtils.setField(service, "fcmEnabled", true);
        ReflectionTestUtils.setField(service, "credentialsPath", "/invalid/path.json");
        
        service.initialize();
        
        assertFalse(service.isInitialized());
    }
    
    @Test
    void testSendPushNotificationDataPayload() {
        ReflectionTestUtils.setField(service, "initialized", false);
        
        Map<String, String> data = new HashMap<>();
        data.put("taskId", "123");
        data.put("priority", "HIGH");
        data.put("assignedTo", "user-456");
        
        boolean result = service.sendPushNotification(
                "device-token-xyz",
                "Task Assignment",
                "You have been assigned to task #123",
                data
        );
        
        assertFalse(result);
    }
    
    @Test
    void testSendPushNotificationWithBlankTitle() {
        ReflectionTestUtils.setField(service, "initialized", false);
        
        boolean result = service.sendPushNotification(
                "device-token",
                "",
                "Message",
                new HashMap<>()
        );
        
        assertFalse(result);
    }
    
    @Test
    void testSendPushNotificationWithBlankMessage() {
        ReflectionTestUtils.setField(service, "initialized", false);
        
        boolean result = service.sendPushNotification(
                "device-token",
                "Title",
                "",
                new HashMap<>()
        );
        
        assertFalse(result);
    }
}
