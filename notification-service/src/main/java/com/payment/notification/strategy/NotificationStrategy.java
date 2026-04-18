package com.payment.notification.strategy;

/**
 * STRATEGY PATTERN — Notification delivery interface.
 * Each notification channel (Email, SMS, Push) implements this.
 */
public interface NotificationStrategy {

    /**
     * Send a notification.
     * 
     * @param recipient email or phone number
     * @param subject the notification subject
     * @param message the notification body
     * @return true if sent successfully
     */
    boolean send(String recipient, String subject, String message);
}
