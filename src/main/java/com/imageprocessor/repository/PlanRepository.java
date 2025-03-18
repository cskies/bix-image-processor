package com.imageprocessor.repository;

import com.imageprocessor.model.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {
    Optional<Plan> findByName(String name);
    Optional<Plan> findByIsPremium(boolean isPremium);
}