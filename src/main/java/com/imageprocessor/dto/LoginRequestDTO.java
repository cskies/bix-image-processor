package com.imageprocessor.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {

    @NotBlank(message = "Username não pode estar em branco")
    private String username;

    @NotBlank(message = "Senha não pode estar em branco")
    private String password;
}