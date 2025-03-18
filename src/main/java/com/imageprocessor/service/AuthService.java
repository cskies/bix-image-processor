package com.imageprocessor.service;

import com.imageprocessor.dto.AuthResponseDTO;
import com.imageprocessor.dto.LoginRequestDTO;
import com.imageprocessor.dto.RegistrationRequestDTO;
import com.imageprocessor.dto.UserDTO;
import com.imageprocessor.exception.ResourceNotFoundException;
import com.imageprocessor.model.Plan;
import com.imageprocessor.model.QuotaUsage;
import com.imageprocessor.model.Subscription;
import com.imageprocessor.model.User;
import com.imageprocessor.repository.PlanRepository;
import com.imageprocessor.repository.UserRepository;
import com.imageprocessor.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Transactional
    public void register(RegistrationRequestDTO registrationRequest) {
        // Verificar se o username já existe
        if (userRepository.existsByUsername(registrationRequest.getUsername())) {
            throw new IllegalArgumentException("Username já está em uso");
        }

        // Verificar se o email já existe
        if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            throw new IllegalArgumentException("Email já está em uso");
        }

        // Verificar se as senhas coincidem
        if (!registrationRequest.getPassword().equals(registrationRequest.getConfirmPassword())) {
            throw new IllegalArgumentException("As senhas não coincidem");
        }

        // Obter plano (padrão é o básico se não for especificado)
        Plan plan = planRepository.findById(registrationRequest.getPlanId())
                .orElseGet(() -> planRepository.findByIsPremium(false)
                        .orElseThrow(() -> new ResourceNotFoundException("Plano básico não encontrado")));

        // Gerar token de verificação
        String verificationToken = UUID.randomUUID().toString();

        // Criar usuário
        User user = User.builder()
                .username(registrationRequest.getUsername())
                .email(registrationRequest.getEmail())
                .password(passwordEncoder.encode(registrationRequest.getPassword()))
                .emailVerified(false)
                .verificationToken(verificationToken)
                .build();

        userRepository.save(user);

        // Criar assinatura
        Subscription subscription = Subscription.builder()
                .user(user)
                .plan(plan)
                .startDate(LocalDateTime.now())
                .endDate(plan.isPremium() ? LocalDateTime.now().plusMonths(1) : null)
                .active(true)
                .build();

        user.setSubscription(subscription);

        // Criar quota de uso
        QuotaUsage quotaUsage = QuotaUsage.builder()
                .user(user)
                .usedToday(0)
                .lastResetDate(LocalDateTime.now())
                .build();

        user.setQuotaUsage(quotaUsage);

        userRepository.save(user);

        // Enviar email de confirmação
        emailService.sendVerificationEmail(user.getEmail(), user.getVerificationToken());
    }

    @Transactional(readOnly = true)
    public AuthResponseDTO login(LoginRequestDTO loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", loginRequest.getUsername()));

        String token = tokenProvider.generateToken(user);

        return AuthResponseDTO.builder()
                .token(token)
                .tokenType("Bearer")
                .user(mapUserToDto(user))
                .build();
    }

    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Token de verificação inválido"));

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);
    }

    private UserDTO mapUserToDto(User user) {
        String planName = "Free";
        boolean isPremium = false;
        Integer quotaTotal = 0;

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
}