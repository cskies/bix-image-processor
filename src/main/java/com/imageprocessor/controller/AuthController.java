package com.imageprocessor.controller;

import com.imageprocessor.dto.AuthResponseDTO;
import com.imageprocessor.dto.LoginRequestDTO;
import com.imageprocessor.dto.RegistrationRequestDTO;
import com.imageprocessor.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API de autenticação para registro e login de usuários")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Registrar novo usuário", description = "Cria uma nova conta de usuário e envia e-mail de confirmação")
    public ResponseEntity<String> register(@Valid @RequestBody RegistrationRequestDTO registrationRequest) {
        authService.register(registrationRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Usuário registrado com sucesso. Por favor, verifique seu e-mail para ativar sua conta.");
    }

    @PostMapping("/login")
    @Operation(summary = "Login de usuário", description = "Autentica o usuário e retorna um token JWT")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        AuthResponseDTO authResponse = authService.login(loginRequest);
        return ResponseEntity.ok(authResponse);
    }

    @GetMapping("/verify")
    @Operation(summary = "Verificar e-mail", description = "Verifica o e-mail do usuário usando o token enviado por e-mail")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok("E-mail verificado com sucesso. Sua conta está ativa.");
    }
}