package com.imageprocessor.service;

import com.imageprocessor.dto.PlanDTO;
import com.imageprocessor.dto.SubscriptionDTO;
import com.imageprocessor.dto.UserDTO;
import com.imageprocessor.exception.ResourceNotFoundException;
import com.imageprocessor.model.Plan;
import com.imageprocessor.model.Subscription;
import com.imageprocessor.model.User;
import com.imageprocessor.repository.PlanRepository;
import com.imageprocessor.repository.SubscriptionRepository;
import com.imageprocessor.repository.UserRepository;
import com.imageprocessor.security.JwtUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Transactional(readOnly = true)
    public UserDTO getCurrentUser() {
        JwtUserDetails userDetails = (JwtUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getId()));

        return mapUserToDto(user);
    }

    @Transactional(readOnly = true)
    public List<PlanDTO> getAllPlans() {
        return planRepository.findAll().stream()
                .map(this::mapPlanToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public SubscriptionDTO updateSubscription(Long planId) {
        JwtUserDetails userDetails = (JwtUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getId()));

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan", "id", planId));

        Subscription subscription = user.getSubscription();
        if (subscription == null) {
            subscription = Subscription.builder()
                    .user(user)
                    .plan(plan)
                    .startDate(LocalDateTime.now())
                    .active(true)
                    .build();
        } else {
            subscription.setPlan(plan);
            subscription.setStartDate(LocalDateTime.now());
            subscription.setActive(true);
        }

        if (plan.isPremium()) {
            subscription.setEndDate(LocalDateTime.now().plusMonths(1));
        } else {
            subscription.setEndDate(null);
        }

        subscriptionRepository.save(subscription);
        return mapSubscriptionToDto(subscription);
    }

    private UserDTO mapUserToDto(User user) {
        String planName = "Free";
        boolean isPremium = false;
        Integer quotaTotal = 10; // Default quota

        if (user.getSubscription() != null && user.getSubscription().isActive()) {
            Plan plan = user.getSubscription().getPlan();
            planName = plan.getName();
            isPremium = plan.isPremium();
            quotaTotal = plan.getDailyQuota();
        }

        Integer quotaUsed = 0;
        if (user.getQuotaUsage() != null) {
            quotaUsed = user.getQuotaUsage().getUsedToday();
        }

        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .emailVerified(user.isEmailVerified())
                .createdAt(user.getCreatedAt())
                .planName(planName)
                .isPremium(isPremium)
                .quotaUsed(quotaUsed)
                .quotaTotal(quotaTotal)
                .build();
    }

    private PlanDTO mapPlanToDto(Plan plan) {
        return PlanDTO.builder()
                .id(plan.getId())
                .name(plan.getName())
                .description(plan.getDescription())
                .isPremium(plan.isPremium())
                .dailyQuota(plan.getDailyQuota())
                .monthlyPrice(plan.getMonthlyPrice())
                .build();
    }

    private SubscriptionDTO mapSubscriptionToDto(Subscription subscription) {
        return SubscriptionDTO.builder()
                .id(subscription.getId())
                .plan(mapPlanToDto(subscription.getPlan()))
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .active(subscription.isActive())
                .build();
    }
}