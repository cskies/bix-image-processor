package com.imageprocessor.controller;

import com.imageprocessor.dto.PlanDTO;
import com.imageprocessor.dto.SubscriptionDTO;
import com.imageprocessor.dto.UserDTO;
import com.imageprocessor.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "API para gestão de usuários e seus planos")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Obter usuário atual", description = "Retorna informações detalhadas sobre o usuário autenticado")
    public ResponseEntity<UserDTO> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    @GetMapping("/plans")
    @Operation(summary = "Listar planos disponíveis", description = "Retorna todos os planos disponíveis para assinatura")
    public ResponseEntity<List<PlanDTO>> getAllPlans() {
        return ResponseEntity.ok(userService.getAllPlans());
    }

    @PostMapping("/subscription/{planId}")
    @Operation(summary = "Atualizar assinatura", description = "Atualiza ou cria uma nova assinatura para o usuário atual")
    public ResponseEntity<SubscriptionDTO> updateSubscription(@PathVariable Long planId) {
        return ResponseEntity.ok(userService.updateSubscription(planId));
    }
}