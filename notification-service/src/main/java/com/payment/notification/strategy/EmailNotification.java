package com.payment.notification.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * STRATEGY PATTERN — Email notification implementation.
 * 
 * MOCK IMPLEMENTATION: Logs the email instead of actually sending.
 * In production, this would use JavaMailSender or a third-party service
 * like SendGrid, AWS SES, etc.
 */
@Component("emailNotification")
public class EmailNotification implements NotificationStrategy {

    private static final Logger log = LoggerFactory.getLogger(EmailNotification.class);

    @Override
    public boolean send(String recipient, String subject, String message) {
        // Simulate email sending
        log.info("═══════════════════════════════════════════════");
        log.info("📧 SENDING EMAIL");
        log.info("To: {}", recipient);
        log.info("Subject: {}", subject);
        log.info("Body: {}", message);
        log.info("═══════════════════════════════════════════════");

        // In production:
        // MimeMessage mimeMessage = mailSender.createMimeMessage();
        // MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        // helper.setTo(recipient);
        // helper.setSubject(subject);
        // helper.setText(message, true); // true = HTML
        // mailSender.send(mimeMessage);

        return true; // Mock always succeeds
    }
}
