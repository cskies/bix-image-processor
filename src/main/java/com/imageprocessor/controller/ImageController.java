package com.imageprocessor.controller;

import com.imageprocessor.dto.ImageDTO;
import com.imageprocessor.exception.ResourceNotFoundException;
import com.imageprocessor.service.ImageService;
import com.imageprocessor.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Tag(name = "Images", description = "API para gerenciamento de imagens")
@SecurityRequirement(name = "Bearer Authentication")
public class ImageController {

    private final ImageService imageService;
    private final StorageService storageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Carregar imagem", description = "Carregar uma nova imagem para o sistema")
    public ResponseEntity<ImageDTO> uploadImage(@RequestParam("file") MultipartFile file) {
        ImageDTO imageDTO = imageService.uploadImage(file);
        return ResponseEntity.ok(imageDTO);
    }

    @GetMapping
    @Operation(summary = "Listar imagens", description = "Listar todas as imagens do usuário atual")
    public ResponseEntity<Page<ImageDTO>> getUserImages(Pageable pageable) {
        Page<ImageDTO> images = imageService.getUserImages(pageable);
        return ResponseEntity.ok(images);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter imagem", description = "Obter informações detalhadas sobre uma imagem específica")
    public ResponseEntity<ImageDTO> getImage(@PathVariable Long id) {
        return ResponseEntity.ok(imageService.getImage(id));
    }

    @GetMapping("/{id}/content")
    @Operation(summary = "Visualizar imagem", description = "Obter o conteúdo da imagem para visualização")
    public ResponseEntity<Resource> getImageContent(@PathVariable Long id) {
        try {
            ImageDTO imageDTO = imageService.getImage(id);
            Path imagePath = storageService.getFilePath(imageDTO.getUrl().substring(imageDTO.getUrl().lastIndexOf('/') + 1));
            Resource resource = new UrlResource(imagePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + imageDTO.getOriginalFilename() + "\"")
                        .contentType(MediaType.parseMediaType(imageDTO.getContentType()))
                        .body(resource);
            } else {
                throw new ResourceNotFoundException("Image content not found for id: " + id);
            }
        } catch (IOException e) {
            throw new ResourceNotFoundException("Could not read image: " + id);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir imagem", description = "Excluir uma imagem específica e seus dados relacionados")
    public ResponseEntity<Void> deleteImage(@PathVariable Long id) {
        imageService.deleteImage(id);
        return ResponseEntity.noContent().build();
    }
}