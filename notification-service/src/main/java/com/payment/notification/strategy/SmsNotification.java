package com.payment.notification.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * STRATEGY PATTERN — SMS notification implementation.
 * MOCK IMPLEMENTATION: Logs the SMS instead of sending.
 */
@Component("smsNotification")
public class SmsNotification implements NotificationStrategy {

    private static final Logger log = LoggerFactory.getLogger(SmsNotification.class);

    @Override
    public boolean send(String recipient, String subject, String message) {
        log.info("═══════════════════════════════════════════════");
        log.info("📱 SENDING SMS");
        log.info("To: {}", recipient);
        log.info("Message: {}", message);
        log.info("═══════════════════════════════════════════════");
        return true;
    }
}
