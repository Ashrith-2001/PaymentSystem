package com.payment.notification.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.common.config.KafkaTopics;
import com.payment.common.enums.NotificationType;
import com.payment.common.event.NotificationEvent;
import com.payment.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for notification events.
 * 
 * Listens to the NOTIFICATION_EVENTS topic and dispatches
 * notifications via the NotificationService.
 */
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventConsumer.class);

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = KafkaTopics.NOTIFICATION_EVENTS, groupId = "notification-service-group")
    public void handleNotificationEvent(String message) {
        try {
            NotificationEvent event = objectMapper.readValue(message, NotificationEvent.class);
            log.info("Received notification event for user {} (type: {})",
                    event.getUserId(), event.getNotificationType());

            NotificationType type = event.getNotificationType() != null
                    ? event.getNotificationType()
                    : NotificationType.EMAIL;

            String recipient = event.getRecipientEmail() != null
                    ? event.getRecipientEmail()
                    : event.getRecipientPhone();

            notificationService.sendNotification(
                    event.getUserId(),
                    type,
                    recipient,
                    event.getSubject(),
                    event.getMessage(),
                    event.getReferenceId());

        } catch (Exception ex) {
            log.error("Failed to process notification event: {}", ex.getMessage(), ex);
        }
    }
}
