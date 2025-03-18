package com.imageprocessor.controller;

import com.imageprocessor.dto.ProcessingRequestDTO;
import com.imageprocessor.dto.ProcessingResponseDTO;
import com.imageprocessor.exception.ResourceNotFoundException;
import com.imageprocessor.service.ProcessingService;
import com.imageprocessor.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/processing")
@RequiredArgsConstructor
@Tag(name = "Processing", description = "API para processamento de imagens")
@SecurityRequirement(name = "Bearer Authentication")
public class ProcessingController {

    private final ProcessingService processingService;
    private final StorageService storageService;

    @PostMapping
    @Operation(summary = "Criar tarefa de processamento", description = "Cria uma nova tarefa para processar uma imagem existente")
    public ResponseEntity<ProcessingResponseDTO> createProcessingTask(
            @Valid @RequestBody ProcessingRequestDTO requestDTO) {
        ProcessingResponseDTO responseDTO = processingService.createProcessingTask(requestDTO);
        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping
    @Operation(summary = "Listar tarefas", description = "Lista todas as tarefas de processamento do usuário atual")
    public ResponseEntity<Page<ProcessingResponseDTO>> getUserProcessingTasks(Pageable pageable) {
        Page<ProcessingResponseDTO> tasks = processingService.getUserProcessingTasks(pageable);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter tarefa", description = "Obtém informações sobre uma tarefa de processamento específica")
    public ResponseEntity<ProcessingResponseDTO> getProcessingTask(@PathVariable Long id) {
        return ResponseEntity.ok(processingService.getProcessingTask(id));
    }

    @GetMapping("/{id}/result")
    @Operation(summary = "Visualizar resultado", description = "Obtém a imagem resultante de uma tarefa de processamento")
    public ResponseEntity<Resource> getProcessingResult(@PathVariable Long id) {
        try {
            ProcessingResponseDTO taskDTO = processingService.getProcessingTask(id);

            if (taskDTO.getResultImageUrl() == null) {
                return ResponseEntity.notFound().build();
            }

            Path imagePath = storageService.getFilePath(taskDTO.getResultImageUrl().substring(
                    taskDTO.getResultImageUrl().lastIndexOf('/') + 1));
            Resource resource = new UrlResource(imagePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "inline; filename=\"" + taskDTO.getOriginalFilename() + "\"")
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(resource);
            } else {
                throw new ResourceNotFoundException("Resultado não encontrado para tarefa: " + id);
            }
        } catch (IOException e) {
            throw new ResourceNotFoundException("Não foi possível ler o resultado: " + id);
        }
    }
}