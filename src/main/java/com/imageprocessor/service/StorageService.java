package com.imageprocessor.service;

import com.imageprocessor.exception.InvalidImageException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class StorageService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public String storeFile(MultipartFile file) {
        // Verificar se o arquivo é válido
        if (file.isEmpty()) {
            throw new InvalidImageException("Não é possível armazenar um arquivo vazio");
        }

        // Verificar se é uma imagem
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new InvalidImageException("O arquivo deve ser uma imagem");
        }

        // Normalizar o nome do arquivo
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown");
        if (originalFilename.contains("..")) {
            throw new InvalidImageException("O nome do arquivo contém um caminho inválido: " + originalFilename);
        }

        // Gerar um novo nome para o arquivo para evitar colisões
        String fileExtension = getFileExtension(originalFilename);
        String newFilename = UUID.randomUUID().toString() + fileExtension;

        try {
            // Criar diretório de upload se não existir
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            // Copiar o arquivo para o diretório de destino
            Path targetLocation = uploadPath.resolve(newFilename);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            log.info("Arquivo armazenado com sucesso: {}", newFilename);
            return newFilename;
        } catch (IOException e) {
            log.error("Falha ao armazenar arquivo: {}", originalFilename, e);
            throw new InvalidImageException("Falha ao armazenar o arquivo: " + e.getMessage());
        }
    }

    public Path getFilePath(String filename) {
        return Paths.get(uploadDir).toAbsolutePath().normalize().resolve(filename);
    }

    public boolean deleteFile(String filename) {
        try {
            Path filePath = getFilePath(filename);
            return Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.error("Falha ao excluir arquivo: {}", filename, e);
            return false;
        }
    }

    private String getFileExtension(String filename) {
        int lastIndex = filename.lastIndexOf('.');
        if (lastIndex == -1) {
            return "";
        }
        return filename.substring(lastIndex);
    }
}