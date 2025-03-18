package com.imageprocessor.service;

import com.imageprocessor.config.RabbitMQConfig;
import com.imageprocessor.dto.ProcessingRequestDTO;
import com.imageprocessor.dto.ProcessingResponseDTO;
import com.imageprocessor.exception.ResourceNotFoundException;
import com.imageprocessor.model.Image;
import com.imageprocessor.model.ProcessingConfig;
import com.imageprocessor.model.ProcessingTask;
import com.imageprocessor.model.User;
import com.imageprocessor.repository.ImageRepository;
import com.imageprocessor.repository.ProcessingTaskRepository;
import com.imageprocessor.repository.UserRepository;
import com.imageprocessor.security.JwtUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessingService {

    private final ProcessingTaskRepository processingTaskRepository;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final QuotaService quotaService;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public ProcessingResponseDTO createProcessingTask(ProcessingRequestDTO requestDTO) {
        // Obter o usuário atual
        JwtUserDetails userDetails = (JwtUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getId()));

        // Verificar quota
        quotaService.checkAndUpdateQuota(user);

        // Obter a imagem
        Image image = imageRepository.findByIdAndOwner(requestDTO.getImageId(), user)
                .orElseThrow(() -> new ResourceNotFoundException("Image", "id", requestDTO.getImageId()));

        // Criar configuração de processamento
        ProcessingConfig config = ProcessingConfig.builder()
                .resizeEnabled(requestDTO.isResizeEnabled())
                .resizePercentage(requestDTO.getResizePercentage())
                .grayscaleEnabled(requestDTO.isGrayscaleEnabled())
                .build();

        // Criar tarefa de processamento
        ProcessingTask task = ProcessingTask.builder()
                .originalImage(image)
                .status(ProcessingTask.TaskStatus.PENDING)
                .processingConfig(config)
                .build();

        config.setProcessingTask(task);
        processingTaskRepository.save(task);

        // Enviar tarefa para a fila de processamento
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_IMAGE_PROCESSING,
                RabbitMQConfig.ROUTING_KEY_IMAGE_PROCESSING,
                task.getId()
        );

        log.info("Tarefa de processamento criada: {} para imagem: {}", task.getId(), image.getId());

        return mapTaskToDto(task);
    }

    @Transactional(readOnly = true)
    public Page<ProcessingResponseDTO> getUserProcessingTasks(Pageable pageable) {
        JwtUserDetails userDetails = (JwtUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return processingTaskRepository.findByOriginalImage_Owner_Id(userDetails.getId(), pageable)
                .map(this::mapTaskToDto);
    }

    @Transactional(readOnly = true)
    public ProcessingResponseDTO getProcessingTask(Long id) {
        JwtUserDetails userDetails = (JwtUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        ProcessingTask task = processingTaskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ProcessingTask", "id", id));

        // Verificar se a tarefa pertence ao usuário
        if (!task.getOriginalImage().getOwner().getId().equals(userDetails.getId())) {
            throw new ResourceNotFoundException("ProcessingTask", "id", id);
        }

        return mapTaskToDto(task);
    }

    @Transactional
    public void updateTaskStatus(Long taskId, ProcessingTask.TaskStatus status, String resultPath, String errorMessage) {
        ProcessingTask task = processingTaskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("ProcessingTask", "id", taskId));

        task.setStatus(status);

        if (status == ProcessingTask.TaskStatus.COMPLETED) {
            task.setResultImagePath(resultPath);
            task.setCompletedAt(LocalDateTime.now());
        } else if (status == ProcessingTask.TaskStatus.FAILED) {
            task.setErrorMessage(errorMessage);
            task.setCompletedAt(LocalDateTime.now());
        }

        processingTaskRepository.save(task);
        log.info("Status da tarefa {} atualizado para: {}", taskId, status);
    }

    private ProcessingResponseDTO mapTaskToDto(ProcessingTask task) {
        String resultImageUrl = null;
        if (task.getResultImagePath() != null) {
            resultImageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/api/processing/")
                    .path(task.getId().toString())
                    .path("/result")
                    .build()
                    .toString();
        }

        ProcessingResponseDTO.ProcessingConfigDTO configDTO = null;
        if (task.getProcessingConfig() != null) {
            configDTO = ProcessingResponseDTO.ProcessingConfigDTO.builder()
                    .resizeEnabled(task.getProcessingConfig().isResizeEnabled())
                    .resizePercentage(task.getProcessingConfig().getResizePercentage())
                    .grayscaleEnabled(task.getProcessingConfig().isGrayscaleEnabled())
                    .build();
        }

        return ProcessingResponseDTO.builder()
                .taskId(task.getId())
                .imageId(task.getOriginalImage().getId())
                .originalFilename(task.getOriginalImage().getOriginalFilename())
                .status(task.getStatus())
                .resultImageUrl(resultImageUrl)
                .errorMessage(task.getErrorMessage())
                .createdAt(task.getCreatedAt())
                .completedAt(task.getCompletedAt())
                .config(configDTO)
                .build();
    }
}