package com.imageprocessor.repository;

import com.imageprocessor.model.QuotaUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuotaUsageRepository extends JpaRepository<QuotaUsage, Long> {
    Optional<QuotaUsage> findByUser_Id(Long userId);

    @Query("SELECT q FROM QuotaUsage q WHERE q.lastResetDate < :resetDate")
    List<QuotaUsage> findAllRequiringReset(@Param("resetDate") LocalDateTime resetDate);
}