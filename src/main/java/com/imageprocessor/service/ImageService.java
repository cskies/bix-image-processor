package com.imageprocessor.service;

import com.imageprocessor.dto.ImageDTO;
import com.imageprocessor.exception.ResourceNotFoundException;
import com.imageprocessor.model.Image;
import com.imageprocessor.model.User;
import com.imageprocessor.repository.ImageRepository;
import com.imageprocessor.repository.UserRepository;
import com.imageprocessor.security.JwtUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final QuotaService quotaService;

    @Value("${app.base-url}")
    private String baseUrl;

    @Transactional
    public ImageDTO uploadImage(MultipartFile file) {
        // Obter o usuário atual
        JwtUserDetails userDetails = (JwtUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getId()));

        // Verificar e atualizar a quota
        quotaService.checkAndUpdateQuota(user);

        // Armazenar o arquivo
        String storagePath = storageService.storeFile(file);

        // Criar entidade de imagem
        Image image = Image.builder()
                .originalFilename(file.getOriginalFilename())
                .storagePath(storagePath)
                .contentType(file.getContentType())
                .size(file.getSize())
                .owner(user)
                .build();

        imageRepository.save(image);
        log.info("Imagem {} carregada pelo usuário: {}", image.getId(), user.getUsername());

        return mapImageToDto(image);
    }

    @Transactional(readOnly = true)
    public Page<ImageDTO> getUserImages(Pageable pageable) {
        JwtUserDetails userDetails = (JwtUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getId()));

        return imageRepository.findByOwner(user, pageable)
                .map(this::mapImageToDto);
    }

    @Transactional(readOnly = true)
    public ImageDTO getImage(Long id) {
        JwtUserDetails userDetails = (JwtUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getId()));

        Image image = imageRepository.findByIdAndOwner(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Image", "id", id));

        return mapImageToDto(image);
    }

    @Transactional
    public void deleteImage(Long id) {
        JwtUserDetails userDetails = (JwtUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getId()));

        Image image = imageRepository.findByIdAndOwner(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Image", "id", id));

        // Excluir arquivo do armazenamento
        storageService.deleteFile(image.getStoragePath());

        // Excluir entidade do banco de dados
        imageRepository.delete(image);
        log.info("Imagem {} excluída pelo usuário: {}", id, user.getUsername());
    }

    private ImageDTO mapImageToDto(Image image) {
        String imageUrl = ServletUriComponentsBuilder.fromUriString(baseUrl)
                .path("/api/images/")
                .path(image.getId().toString())
                .path("/content")
                .build()
                .toString();

        return ImageDTO.builder()
                .id(image.getId())
                .originalFilename(image.getOriginalFilename())
                .contentType(image.getContentType())
                .size(image.getSize())
                .url(imageUrl)
                .createdAt(image.getCreatedAt())
                .build();
    }
}