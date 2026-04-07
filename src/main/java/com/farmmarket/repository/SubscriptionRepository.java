package com.farmmarket.repository;

import com.farmmarket.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    Optional<Subscription> findByUserId(Long userId);

    List<Subscription> findByStatus(Subscription.SubscriptionStatus status);

    List<Subscription> findByUpgradeRequestStatus(Subscription.UpgradeStatus upgradeStatus);

    Boolean existsByUserId(Long userId);
}
