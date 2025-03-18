package com.imageprocessor.queue;

import com.imageprocessor.config.RabbitMQConfig;
import com.imageprocessor.exception.ResourceNotFoundException;
import com.imageprocessor.model.Image;
import com.imageprocessor.model.ProcessingConfig;
import com.imageprocessor.model.ProcessingTask;
import com.imageprocessor.model.User;
import com.imageprocessor.repository.ProcessingTaskRepository;
import com.imageprocessor.service.EmailService;
import com.imageprocessor.service.ProcessingService;
import com.imageprocessor.service.StorageService;
import com.imageprocessor.util.ImageProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ImageProcessingConsumer {

    private final ProcessingTaskRepository processingTaskRepository;
    private final ProcessingService processingService;
    private final StorageService storageService;
    private final ImageProcessor imageProcessor;
    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_IMAGE_PROCESSING)
    public void processImage(Long taskId) {
        log.info("Recebida tarefa de processamento: {}", taskId);

        try {
            // Obter a tarefa
            ProcessingTask task = processingTaskRepository.findById(taskId)
                    .orElseThrow(() -> new ResourceNotFoundException("ProcessingTask", "id", taskId));

            // Atualizar status para "em processamento"
            processingService.updateTaskStatus(taskId, ProcessingTask.TaskStatus.PROCESSING, null, null);

            // Obter informações necessárias
            Image originalImage = task.getOriginalImage();
            User user = originalImage.getOwner();
            ProcessingConfig config = task.getProcessingConfig();

            // Obter caminhos dos arquivos
            Path originalPath = storageService.getFilePath(originalImage.getStoragePath());
            String resultFilename = UUID.randomUUID().toString() + getFileExtension(originalImage.getOriginalFilename());
            Path resultPath = storageService.getFilePath(resultFilename);

            // Processar imagem conforme configuração
            if (config.isResizeEnabled() && config.isGrayscaleEnabled()) {
                // Primeiro redimensionar, depois converter para escala de cinza
                Path tempPath = storageService.getFilePath("temp-" + resultFilename);
                imageProcessor.resizeImage(originalPath, tempPath, config.getResizePercentage());
                imageProcessor.convertToGrayscale(tempPath, resultPath);
                storageService.deleteFile("temp-" + resultFilename);
            } else if (config.isResizeEnabled()) {
                // Apenas redimensionar
                imageProcessor.resizeImage(originalPath, resultPath, config.getResizePercentage());
            } else if (config.isGrayscaleEnabled()) {
                // Apenas converter para escala de cinza
                imageProcessor.convertToGrayscale(originalPath, resultPath);
            } else {
                // Nenhum processamento solicitado
                throw new IllegalArgumentException("Nenhuma operação de processamento solicitada");
            }

            // Atualizar status para "concluído"
            processingService.updateTaskStatus(taskId, ProcessingTask.TaskStatus.COMPLETED, resultFilename, null);

            // Enviar e-mail de notificação
            String resultUrl = ""; // Gerar URL para acesso ao resultado
            emailService.sendProcessingCompletedEmail(user.getEmail(), originalImage.getOriginalFilename(), resultUrl);

            log.info("Processamento da tarefa {} concluído com sucesso", taskId);
        } catch (Exception e) {
            log.error("Erro ao processar imagem para tarefa: {}", taskId, e);
            // Atualizar status para "falha"
            processingService.updateTaskStatus(taskId, ProcessingTask.TaskStatus.FAILED, null, e.getMessage());
        }
    }

    private String getFileExtension(String filename) {
        int lastIndex = filename.lastIndexOf('.');
        if (lastIndex == -1) {
            return ".jpg"; // Padrão se não encontrar extensão
        }
        return filename.substring(lastIndex);
    }
}