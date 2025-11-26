package com.fsm.notification.application.service;

import com.fsm.notification.domain.model.Notification;
import com.fsm.notification.domain.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for NotificationService.
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {
    
    @Mock
    private NotificationRepository notificationRepository;
    
    @Mock
    private PushNotificationService pushNotificationService;
    
    @InjectMocks
    private NotificationService notificationService;
    
    private Notification testNotification;
    
    @BeforeEach
    void setUp() {
        testNotification = Notification.builder()
                .id(1L)
                .userId(101L)
                .type(Notification.NotificationType.PUSH)
                .title("Test Title")
                .message("Test Message")
                .data("{\"key\": \"value\"}")
                .build();
    }
    
    @Test
    void testSendPushNotificationSuccess() {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        when(pushNotificationService.sendPushNotification(anyString(), anyString(), anyString(), anyMap()))
                .thenReturn(true);
        
        // Act
        Notification result = notificationService.sendPushNotification(
                101L,
                "test-device-token",
                "Test Title",
                "Test Message",
                "{\"key\": \"value\"}"
        );
        
        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(101L, result.getUserId());
        assertEquals("Test Title", result.getTitle());
        assertEquals("Test Message", result.getMessage());
        
        verify(notificationRepository, times(2)).save(any(Notification.class));
        verify(pushNotificationService, times(1)).sendPushNotification(anyString(), anyString(), anyString(), anyMap());
    }
    
    @Test
    void testSendPushNotificationFailure() {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        when(pushNotificationService.sendPushNotification(anyString(), anyString(), anyString(), anyMap()))
                .thenReturn(false);
        
        // Act
        Notification result = notificationService.sendPushNotification(
                101L,
                "test-device-token",
                "Test Title",
                "Test Message",
                "{\"key\": \"value\"}"
        );
        
        // Assert
        assertNotNull(result);
        verify(notificationRepository, times(2)).save(any(Notification.class));
        verify(pushNotificationService, times(1)).sendPushNotification(anyString(), anyString(), anyString(), anyMap());
    }
    
    @Test
    void testSendPushNotificationWithNullData() {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        when(pushNotificationService.sendPushNotification(anyString(), anyString(), anyString(), anyMap()))
                .thenReturn(true);
        
        // Act
        Notification result = notificationService.sendPushNotification(
                101L,
                "test-device-token",
                "Test Title",
                "Test Message",
                null
        );
        
        // Assert
        assertNotNull(result);
        verify(pushNotificationService, times(1)).sendPushNotification(anyString(), anyString(), anyString(), anyMap());
    }
    
    @Test
    void testSendPushNotificationWithEmptyData() {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        when(pushNotificationService.sendPushNotification(anyString(), anyString(), anyString(), anyMap()))
                .thenReturn(true);
        
        // Act
        Notification result = notificationService.sendPushNotification(
                101L,
                "test-device-token",
                "Test Title",
                "Test Message",
                ""
        );
        
        // Assert
        assertNotNull(result);
        verify(pushNotificationService, times(1)).sendPushNotification(anyString(), anyString(), anyString(), anyMap());
    }
    
    @Test
    void testSendPushNotificationDataPayloadContainsNotificationId() {
        // Arrange
        ArgumentCaptor<Map<String, String>> dataCaptor = ArgumentCaptor.forClass(Map.class);
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        when(pushNotificationService.sendPushNotification(anyString(), anyString(), anyString(), dataCaptor.capture()))
                .thenReturn(true);
        
        // Act
        notificationService.sendPushNotification(
                101L,
                "test-device-token",
                "Test Title",
                "Test Message",
                "{\"key\": \"value\"}"
        );
        
        // Assert
        Map<String, String> capturedData = dataCaptor.getValue();
        assertTrue(capturedData.containsKey("notificationId"));
        assertEquals("1", capturedData.get("notificationId"));
    }
    
    @Test
    void testSendPushNotificationMarksAsSentAndDeliveredOnSuccess() {
        // Arrange
        Notification savedNotification = Notification.builder()
                .id(1L)
                .userId(101L)
                .type(Notification.NotificationType.PUSH)
                .title("Test Title")
                .message("Test Message")
                .build();
        
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);
        when(pushNotificationService.sendPushNotification(anyString(), anyString(), anyString(), anyMap()))
                .thenReturn(true);
        
        // Act
        Notification result = notificationService.sendPushNotification(
                101L,
                "test-device-token",
                "Test Title",
                "Test Message",
                null
        );
        
        // Assert
        assertNotNull(result);
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }
    
    @Test
    void testSendPushNotificationMarksAsSentOnFailure() {
        // Arrange
        Notification savedNotification = Notification.builder()
                .id(1L)
                .userId(101L)
                .type(Notification.NotificationType.PUSH)
                .title("Test Title")
                .message("Test Message")
                .build();
        
        when(notificationRepository.save(any(Notification.class))).thenReturn(savedNotification);
        when(pushNotificationService.sendPushNotification(anyString(), anyString(), anyString(), anyMap()))
                .thenReturn(false);
        
        // Act
        Notification result = notificationService.sendPushNotification(
                101L,
                "test-device-token",
                "Test Title",
                "Test Message",
                null
        );
        
        // Assert
        assertNotNull(result);
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }
    
    @Test
    void testSendPushNotificationWithRetrySuccess() {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        when(pushNotificationService.sendPushNotification(anyString(), anyString(), anyString(), anyMap()))
                .thenReturn(true);
        
        // Mock notification behavior
        testNotification.markAsSent();
        testNotification.markAsDelivered();
        
        // Act
        Notification result = notificationService.sendPushNotificationWithRetry(
                101L,
                "test-device-token",
                "Test Title",
                "Test Message",
                "{\"key\": \"value\"}",
                3
        );
        
        // Assert
        assertNotNull(result);
        verify(pushNotificationService, atLeastOnce()).sendPushNotification(anyString(), anyString(), anyString(), anyMap());
    }
    
    @Test
    void testSendPushNotificationWithRetryFailureAfterMaxAttempts() {
        // Arrange
        Notification failedNotification = Notification.builder()
                .id(1L)
                .userId(101L)
                .type(Notification.NotificationType.PUSH)
                .title("Test Title")
                .message("Test Message")
                .build();
        
        when(notificationRepository.save(any(Notification.class))).thenReturn(failedNotification);
        when(pushNotificationService.sendPushNotification(anyString(), anyString(), anyString(), anyMap()))
                .thenReturn(false);
        
        // Act
        Notification result = notificationService.sendPushNotificationWithRetry(
                101L,
                "test-device-token",
                "Test Title",
                "Test Message",
                null,
                3
        );
        
        // Assert
        assertNotNull(result);
        verify(pushNotificationService, times(3)).sendPushNotification(anyString(), anyString(), anyString(), anyMap());
    }
    
    @Test
    void testSendPushNotificationWithRetryZeroMaxRetries() {
        // Act
        Notification result = notificationService.sendPushNotificationWithRetry(
                101L,
                "test-device-token",
                "Test Title",
                "Test Message",
                null,
                0
        );
        
        // Assert - with 0 max retries, the loop doesn't execute
        assertNull(result);
        verify(pushNotificationService, never()).sendPushNotification(anyString(), anyString(), anyString(), anyMap());
        verify(notificationRepository, never()).save(any(Notification.class));
    }
    
    @Test
    void testIsPushNotificationServiceReady() {
        // Arrange
        when(pushNotificationService.isInitialized()).thenReturn(true);
        
        // Act
        boolean result = notificationService.isPushNotificationServiceReady();
        
        // Assert
        assertTrue(result);
        verify(pushNotificationService, times(1)).isInitialized();
    }
    
    @Test
    void testIsPushNotificationServiceNotReady() {
        // Arrange
        when(pushNotificationService.isInitialized()).thenReturn(false);
        
        // Act
        boolean result = notificationService.isPushNotificationServiceReady();
        
        // Assert
        assertFalse(result);
        verify(pushNotificationService, times(1)).isInitialized();
    }
}
