package com.payment.notification.entity;

import com.payment.common.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * NotificationLog — audit trail of all sent notifications.
 */
@Entity
@Table(name = "notification_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private NotificationType type;

    private String recipient;  // email address or phone number

    @Column(length = 200)
    private String subject;

    @Column(length = 2000)
    private String message;

    /** Reference to triggering entity (orderId, paymentId, etc.) */
    @Column(length = 100)
    private String referenceId;

    @Column(length = 20)
    @Builder.Default
    private String status = "SENT";  // SENT, FAILED

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime sentAt;
}
