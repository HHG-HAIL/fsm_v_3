package com.fsm.notification.application.service;

import com.fsm.notification.domain.model.Notification;
import com.fsm.notification.domain.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Notification service handling notification creation and delivery.
 * 
 * Domain Invariants:
 * - Notifications must have a valid user ID
 * - Device tokens must be associated with user
 * - Failed deliveries are logged for troubleshooting
 * - Notification status is tracked (sent, delivered)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final PushNotificationService pushNotificationService;
    
    /**
     * Send a push notification to a user
     * 
     * @param userId User ID to send notification to
     * @param deviceToken User's device token
     * @param title Notification title
     * @param message Notification message
     * @param data Additional data payload (optional)
     * @return The created notification entity
     */
    @Transactional
    public Notification sendPushNotification(Long userId, String deviceToken, String title, 
                                            String message, String data) {
        log.info("Sending push notification to user {}", userId);
        
        // Create notification entity
        Notification notification = Notification.builder()
                .userId(userId)
                .type(Notification.NotificationType.PUSH)
                .title(title)
                .message(message)
                .data(data)
                .build();
        
        // Save notification to database
        notification = notificationRepository.save(notification);
        
        // Convert data string to map for FCM
        Map<String, String> dataMap = new HashMap<>();
        if (data != null && !data.trim().isEmpty()) {
            dataMap.put("data", data);
        }
        dataMap.put("notificationId", String.valueOf(notification.getId()));
        
        // Send push notification
        boolean sent = pushNotificationService.sendPushNotification(deviceToken, title, message, dataMap);
        
        if (sent) {
            notification.markAsSent();
            notification.markAsDelivered();
            notificationRepository.save(notification);
            log.info("Push notification sent and marked as delivered for notification ID: {}", 
                    notification.getId());
        } else {
            notification.markAsSent(); // Mark as sent even if failed (attempted)
            notificationRepository.save(notification);
            log.warn("Push notification failed to deliver for notification ID: {}", 
                    notification.getId());
        }
        
        return notification;
    }
    
    /**
     * Send a push notification with retry logic
     * 
     * @param userId User ID to send notification to
     * @param deviceToken User's device token
     * @param title Notification title
     * @param message Notification message
     * @param data Additional data payload (optional)
     * @param maxRetries Maximum number of retry attempts
     * @return The created notification entity
     */
    @Transactional
    public Notification sendPushNotificationWithRetry(Long userId, String deviceToken, String title, 
                                                     String message, String data, int maxRetries) {
        log.info("Sending push notification to user {} with retry (max attempts: {})", userId, maxRetries);
        
        Notification notification = null;
        boolean sent = false;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            log.debug("Attempt {} of {} to send push notification", attempt, maxRetries);
            
            try {
                notification = sendPushNotification(userId, deviceToken, title, message, data);
                
                if (notification.isSent() && notification.isDelivered()) {
                    sent = true;
                    break;
                }
                
                // Wait before retry (exponential backoff)
                if (attempt < maxRetries) {
                    Thread.sleep(1000L * attempt);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Push notification retry interrupted", e);
                break;
            } catch (Exception e) {
                log.error("Error during push notification attempt {}: {}", attempt, e.getMessage());
            }
        }
        
        if (!sent) {
            log.error("Failed to send push notification after {} attempts", maxRetries);
        }
        
        return notification;
    }
    
    /**
     * Check if push notification service is ready
     * 
     * @return true if service is initialized, false otherwise
     */
    public boolean isPushNotificationServiceReady() {
        return pushNotificationService.isInitialized();
    }
}
