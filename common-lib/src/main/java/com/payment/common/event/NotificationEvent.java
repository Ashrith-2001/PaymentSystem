package com.payment.common.event;

import com.payment.common.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Event published to trigger notifications.
 * Consumed by: Notification Service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NotificationEvent extends BaseEvent {

    private Long userId;
    private String recipientEmail;
    private String recipientPhone;
    private NotificationType notificationType;
    private String subject;
    private String message;

    /** Reference ID (orderId, paymentId, etc.) for context */
    private String referenceId;

    /**
     * Factory: Create email notification event.
     */
    public static NotificationEvent emailNotification(Long userId, String email,
                                                        String subject, String message,
                                                        String referenceId) {
        NotificationEvent event = new NotificationEvent();
        event.initBaseEvent("NOTIFICATION_REQUESTED", "order-service");
        event.setUserId(userId);
        event.setRecipientEmail(email);
        event.setNotificationType(NotificationType.EMAIL);
        event.setSubject(subject);
        event.setMessage(message);
        event.setReferenceId(referenceId);
        return event;
    }
}
