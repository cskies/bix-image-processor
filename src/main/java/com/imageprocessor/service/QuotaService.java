package com.imageprocessor.service;

import com.imageprocessor.exception.QuotaExceededException;
import com.imageprocessor.model.QuotaUsage;
import com.imageprocessor.model.Subscription;
import com.imageprocessor.model.User;
import com.imageprocessor.repository.QuotaUsageRepository;
import com.imageprocessor.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuotaService {

    private final QuotaUsageRepository quotaUsageRepository;
    private final UserRepository userRepository;

    @Transactional
    public void checkAndUpdateQuota(User user) {
        // Se o usuário tem plano premium, não verificamos a quota
        Subscription subscription = user.getSubscription();
        if (subscription != null && subscription.isActive() && subscription.getPlan().isPremium()) {
            log.debug("Usuário premium: {} - sem verificação de quota", user.getUsername());
            return;
        }

        // Obter a quota do usuário
        QuotaUsage quotaUsage = user.getQuotaUsage();
        if (quotaUsage == null) {
            quotaUsage = QuotaUsage.builder()
                    .user(user)
                    .usedToday(0)
                    .lastResetDate(LocalDateTime.now())
                    .build();
            user.setQuotaUsage(quotaUsage);
        }

        // Verificar se a quota diária já foi reiniciada hoje
        LocalDateTime startOfToday = LocalDateTime.now().toLocalDate().atStartOfDay();
        if (quotaUsage.getLastResetDate().isBefore(startOfToday)) {
            quotaUsage.setUsedToday(0);
            quotaUsage.setLastResetDate(LocalDateTime.now());
        }

        // Verificar se a quota foi excedida
        int dailyLimit = subscription != null && subscription.getPlan() != null
                ? subscription.getPlan().getDailyQuota()
                : 5; // Limite padrão se não tiver plano

        if (quotaUsage.getUsedToday() >= dailyLimit) {
            throw new QuotaExceededException(quotaUsage.getUsedToday(), dailyLimit);
        }

        // Incrementar a quota utilizada
        quotaUsage.setUsedToday(quotaUsage.getUsedToday() + 1);
        quotaUsageRepository.save(quotaUsage);

        log.debug("Quota atualizada para usuário: {} - {}/{}",
                user.getUsername(), quotaUsage.getUsedToday(), dailyLimit);
    }

    @Scheduled(cron = "0 0 0 * * ?", zone = "UTC") // Executar todos os dias à meia-noite UTC
    @Transactional
    public void resetDailyQuotas() {
        LocalDateTime resetTime = LocalDateTime.now(ZoneOffset.UTC);
        List<QuotaUsage> quotasToReset = quotaUsageRepository.findAll();

        int count = 0;
        for (QuotaUsage quota : quotasToReset) {
            quota.setUsedToday(0);
            quota.setLastResetDate(resetTime);
            quotaUsageRepository.save(quota);
            count++;
        }

        log.info("Resetadas {} quotas diárias em {}", count, resetTime);
    }
}