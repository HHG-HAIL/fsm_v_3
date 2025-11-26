package com.fsm.notification.application.service;

import java.util.Map;

/**
 * Interface for push notification services.
 * Provides abstraction for different push notification providers (FCM, AWS SNS, etc.)
 */
public interface PushNotificationService {
    
    /**
     * Send a push notification to a device
     * 
     * @param deviceToken The device token to send the notification to
     * @param title The notification title
     * @param message The notification message/body
     * @param data Additional data payload
     * @return true if notification was sent successfully, false otherwise
     */
    boolean sendPushNotification(String deviceToken, String title, String message, Map<String, String> data);
    
    /**
     * Check if the push notification service is initialized and ready
     * 
     * @return true if service is ready, false otherwise
     */
    boolean isInitialized();
}
