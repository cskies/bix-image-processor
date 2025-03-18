package com.imageprocessor.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String email;
    private boolean emailVerified;
    private LocalDateTime createdAt;
    private String planName;
    private boolean isPremium;
    private Integer quotaUsed;
    private Integer quotaTotal;
}