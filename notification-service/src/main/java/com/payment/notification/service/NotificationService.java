package com.payment.notification.service;

import com.payment.common.enums.NotificationType;
import com.payment.notification.entity.NotificationLog;
import com.payment.notification.repository.NotificationLogRepository;
import com.payment.notification.strategy.EmailNotification;
import com.payment.notification.strategy.NotificationStrategy;
import com.payment.notification.strategy.SmsNotification;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.Map;

/**
 * Notification Service — dispatches notifications using the Strategy pattern.
 * Resolves the correct notification strategy based on NotificationType.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationLogRepository logRepository;
    private final Map<NotificationType, NotificationStrategy> strategies;

    public NotificationService(NotificationLogRepository logRepository,
                                EmailNotification emailNotification,
                                SmsNotification smsNotification) {
        this.logRepository = logRepository;
        this.strategies = new EnumMap<>(NotificationType.class);
        this.strategies.put(NotificationType.EMAIL, emailNotification);
        this.strategies.put(NotificationType.SMS, smsNotification);
    }

    /**
     * Send a notification via the appropriate channel and log it.
     */
    @Transactional
    public void sendNotification(Long userId, NotificationType type, String recipient,
                                   String subject, String message, String referenceId) {
        log.info("Sending {} notification to user {} (ref: {})", type, userId, referenceId);

        NotificationStrategy strategy = strategies.get(type);
        if (strategy == null) {
            log.error("No strategy found for notification type: {}", type);
            return;
        }

        boolean success = strategy.send(recipient, subject, message);

        // Log the notification for audit
        NotificationLog notificationLog = NotificationLog.builder()
                .userId(userId)
                .type(type)
                .recipient(recipient != null ? recipient : "N/A")
                .subject(subject)
                .message(message)
                .referenceId(referenceId)
                .status(success ? "SENT" : "FAILED")
                .build();

        logRepository.save(notificationLog);
        log.info("Notification {} logged (status: {})", notificationLog.getId(),
                notificationLog.getStatus());
    }
}
