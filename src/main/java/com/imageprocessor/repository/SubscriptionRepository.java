package com.imageprocessor.repository;

import com.imageprocessor.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByUser_Id(Long userId);

    @Query("SELECT s FROM Subscription s WHERE s.active = true AND s.endDate < :currentDate")
    List<Subscription> findExpiredSubscriptions(@Param("currentDate") LocalDateTime currentDate);

    @Query("SELECT s FROM Subscription s JOIN s.plan p WHERE s.active = true AND p.isPremium = true")
    List<Subscription> findAllActivePremiumSubscriptions();
}