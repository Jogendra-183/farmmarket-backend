package com.farmmarket.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PlanType planType = PlanType.BASIC;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    @Column(name = "monthly_price", precision = 10, scale = 2)
    private BigDecimal monthlyPrice;

    @Column(name = "next_billing_date")
    private LocalDateTime nextBillingDate;

    @Column(name = "upgrade_request_status")
    @Enumerated(EnumType.STRING)
    private UpgradeStatus upgradeRequestStatus;

    @Column(name = "requested_plan")
    @Enumerated(EnumType.STRING)
    private PlanType requestedPlan;

    @Column(name = "billing_address")
    private String billingAddress;

    @Column(name = "card_last_four")
    private String cardLastFour;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum PlanType {
        BASIC, PREMIUM, ENTERPRISE
    }

    public enum SubscriptionStatus {
        ACTIVE, CANCELLED, EXPIRED, PENDING
    }

    public enum UpgradeStatus {
        PENDING, APPROVED, REJECTED
    }
}
