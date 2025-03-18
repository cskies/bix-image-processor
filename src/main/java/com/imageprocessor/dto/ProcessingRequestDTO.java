package com.imageprocessor.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessingRequestDTO {

    @NotNull(message = "ID da imagem não pode ser nulo")
    private Long imageId;

    private boolean resizeEnabled;

    @Min(value = 1, message = "Porcentagem de redimensionamento deve ser no mínimo 1%")
    @Max(value = 100, message = "Porcentagem de redimensionamento deve ser no máximo 100%")
    private Integer resizePercentage;

    private boolean grayscaleEnabled;
}