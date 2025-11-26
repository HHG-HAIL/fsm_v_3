package com.fsm.notification.application.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Firebase Cloud Messaging implementation of push notification service.
 * Handles initialization and message sending via FCM.
 * 
 * Domain Invariants:
 * - Device tokens must be valid FCM tokens
 * - Failed deliveries are logged for troubleshooting
 * - Payload size must be within FCM limits (4KB for data messages)
 */
@Service
@Slf4j
public class FcmPushNotificationService implements PushNotificationService {
    
    @Value("${fcm.credentials.path:#{null}}")
    private String credentialsPath;
    
    @Value("${fcm.enabled:false}")
    private boolean fcmEnabled;
    
    private boolean initialized = false;
    
    /**
     * Initialize Firebase App on startup if enabled
     */
    @PostConstruct
    public void initialize() {
        if (!fcmEnabled) {
            log.info("FCM is disabled. Notifications will be logged but not sent.");
            return;
        }
        
        if (credentialsPath == null || credentialsPath.trim().isEmpty()) {
            log.warn("FCM credentials path not configured. Push notifications will not be sent.");
            return;
        }
        
        try {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(new FileInputStream(credentialsPath)))
                    .build();
            
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
            
            initialized = true;
            log.info("Firebase Cloud Messaging initialized successfully");
        } catch (IOException e) {
            log.error("Failed to initialize Firebase: {}", e.getMessage(), e);
        }
    }
    
    @Override
    public boolean sendPushNotification(String deviceToken, String title, String message, Map<String, String> data) {
        if (deviceToken == null || deviceToken.trim().isEmpty()) {
            log.warn("Cannot send push notification: device token is null or empty");
            return false;
        }
        
        if (!initialized) {
            log.warn("FCM not initialized. Logging notification instead: title={}, message={}, token={}", 
                    title, message, maskToken(deviceToken));
            return false;
        }
        
        try {
            // Build the message
            Message.Builder messageBuilder = Message.builder()
                    .setToken(deviceToken)
                    .putData("title", title)
                    .putData("body", message);
            
            // Add additional data if provided
            if (data != null && !data.isEmpty()) {
                data.forEach(messageBuilder::putData);
            }
            
            // Send the message
            String response = FirebaseMessaging.getInstance().send(messageBuilder.build());
            log.info("Push notification sent successfully. Message ID: {}", response);
            return true;
            
        } catch (Exception e) {
            log.error("Failed to send push notification to token {}: {}", 
                    maskToken(deviceToken), e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Mask device token for logging (show first 8 and last 4 characters)
     */
    private String maskToken(String token) {
        if (token == null || token.length() <= 12) {
            return "***";
        }
        return token.substring(0, 8) + "..." + token.substring(token.length() - 4);
    }
}
