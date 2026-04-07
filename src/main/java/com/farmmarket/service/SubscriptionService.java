package com.farmmarket.service;

import com.farmmarket.dto.request.SubscriptionUpgradeRequest;
import com.farmmarket.entity.Notification;
import com.farmmarket.entity.Subscription;
import com.farmmarket.entity.User;
import com.farmmarket.exception.BadRequestException;
import com.farmmarket.exception.ResourceNotFoundException;
import com.farmmarket.repository.NotificationRepository;
import com.farmmarket.repository.SubscriptionRepository;
import com.farmmarket.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    private static final BigDecimal BASIC_PRICE = BigDecimal.ZERO;
    private static final BigDecimal PREMIUM_PRICE = new BigDecimal("19.99");
    private static final BigDecimal ENTERPRISE_PRICE = new BigDecimal("49.99");

    public SubscriptionResponse getSubscription(Long userId) {
        Subscription subscription = subscriptionRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", "userId", userId));
        return mapToResponse(subscription);
    }

    @Transactional
    public SubscriptionResponse requestUpgrade(Long userId, SubscriptionUpgradeRequest request) {
        Subscription subscription = subscriptionRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", "userId", userId));

        if (subscription.getPlanType() == request.getPlanType()) {
            throw new BadRequestException("You are already on this plan");
        }

        subscription.setRequestedPlan(request.getPlanType());
        subscription.setUpgradeRequestStatus(Subscription.UpgradeStatus.PENDING);
        subscription.setBillingAddress(request.getBillingAddress());
        if (request.getCardLastFour() != null && !request.getCardLastFour().isBlank()) {
            subscription.setCardLastFour(request.getCardLastFour());
        }

        Subscription saved = subscriptionRepository.save(subscription);

        // Notify admins (simplified: create a system notification for the user)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        createNotification(user,
                "Upgrade Request Submitted",
                "Your request to upgrade to " + request.getPlanType() + " plan is pending admin approval.",
                Notification.NotificationType.SUBSCRIPTION_UPGRADED, userId);

        return mapToResponse(saved);
    }

    @Transactional
    public SubscriptionResponse approveUpgrade(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", "id", subscriptionId));

        if (subscription.getUpgradeRequestStatus() != Subscription.UpgradeStatus.PENDING) {
            throw new BadRequestException("No pending upgrade request");
        }

        subscription.setPlanType(subscription.getRequestedPlan());
        subscription.setMonthlyPrice(getPlanPrice(subscription.getRequestedPlan()));
        subscription.setUpgradeRequestStatus(Subscription.UpgradeStatus.APPROVED);
        subscription.setRequestedPlan(null);
        subscription.setNextBillingDate(LocalDateTime.now().plusMonths(1));
        subscription.setStatus(Subscription.SubscriptionStatus.ACTIVE);

        Subscription saved = subscriptionRepository.save(subscription);

        // Notify buyer
        createNotification(saved.getUser(),
                "Subscription Upgrade Approved!",
                "Your subscription has been upgraded to " + saved.getPlanType() + " plan.",
                Notification.NotificationType.SUBSCRIPTION_UPGRADED, subscriptionId);

        return mapToResponse(saved);
    }

    @Transactional
    public SubscriptionResponse cancelSubscription(Long userId) {
        Subscription subscription = subscriptionRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", "userId", userId));

        subscription.setStatus(Subscription.SubscriptionStatus.CANCELLED);
        Subscription saved = subscriptionRepository.save(subscription);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        createNotification(user,
                "Subscription Cancelled",
                "Your subscription has been cancelled. It will remain active until the end of your billing period.",
                Notification.NotificationType.SUBSCRIPTION_CANCELLED, userId);

        return mapToResponse(saved);
    }

    public List<SubscriptionResponse> getPendingUpgradeRequests() {
        return subscriptionRepository.findByUpgradeRequestStatus(Subscription.UpgradeStatus.PENDING)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<SubscriptionResponse> getAllSubscriptions() {
        return subscriptionRepository.findAll()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private BigDecimal getPlanPrice(Subscription.PlanType plan) {
        return switch (plan) {
            case BASIC -> BASIC_PRICE;
            case PREMIUM -> PREMIUM_PRICE;
            case ENTERPRISE -> ENTERPRISE_PRICE;
        };
    }

    private void createNotification(User user, String title, String message,
                                    Notification.NotificationType type, Long relatedId) {
        notificationRepository.save(Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .relatedEntityId(relatedId)
                .relatedEntityType("SUBSCRIPTION")
                .build());
    }

    public SubscriptionResponse mapToResponse(Subscription sub) {
        return SubscriptionResponse.builder()
                .id(sub.getId())
                .userId(sub.getUser().getId())
                .userName(sub.getUser().getName())
                .planType(sub.getPlanType())
                .status(sub.getStatus())
                .monthlyPrice(sub.getMonthlyPrice())
                .nextBillingDate(sub.getNextBillingDate())
                .upgradeRequestStatus(sub.getUpgradeRequestStatus())
                .requestedPlan(sub.getRequestedPlan())
                .cardLastFour(sub.getCardLastFour())
                .createdAt(sub.getCreatedAt())
                .build();
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SubscriptionResponse {
        private Long id;
        private Long userId;
        private String userName;
        private Subscription.PlanType planType;
        private Subscription.SubscriptionStatus status;
        private BigDecimal monthlyPrice;
        private LocalDateTime nextBillingDate;
        private Subscription.UpgradeStatus upgradeRequestStatus;
        private Subscription.PlanType requestedPlan;
        private String cardLastFour;
        private LocalDateTime createdAt;
    }
}
