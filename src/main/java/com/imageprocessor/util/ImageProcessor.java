package com.imageprocessor.util;

import com.imageprocessor.exception.InvalidImageException;
import lombok.extern.slf4j.Slf4j;
import org.imgscalr.Scalr;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@Component
@Slf4j
public class ImageProcessor {

    public void resizeImage(Path sourcePath, Path targetPath, int percentage) throws IOException {
        try {
            BufferedImage originalImage = ImageIO.read(sourcePath.toFile());
            if (originalImage == null) {
                throw new InvalidImageException("Não foi possível ler a imagem: " + sourcePath);
            }

            int targetWidth = originalImage.getWidth() * percentage / 100;
            int targetHeight = originalImage.getHeight() * percentage / 100;

            BufferedImage resizedImage = Scalr.resize(originalImage, Scalr.Method.QUALITY, targetWidth, targetHeight);

            String extension = getFileExtension(targetPath.getFileName().toString());
            boolean success = ImageIO.write(resizedImage, extension, targetPath.toFile());

            if (!success) {
                throw new InvalidImageException("Formato de imagem não suportado para escrita: " + extension);
            }
        } catch (IOException e) {
            log.error("Erro ao redimensionar imagem: {}", sourcePath, e);
            throw e;
        }
    }

    public void convertToGrayscale(Path sourcePath, Path targetPath) throws IOException {
        try {
            BufferedImage originalImage = ImageIO.read(sourcePath.toFile());
            if (originalImage == null) {
                throw new InvalidImageException("Não foi possível ler a imagem: " + sourcePath);
            }

            BufferedImage grayscaleImage = new BufferedImage(
                    originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

            ColorConvertOp op = new ColorConvertOp(
                    originalImage.getColorModel().getColorSpace(),
                    grayscaleImage.getColorModel().getColorSpace(),
                    null);

            op.filter(originalImage, grayscaleImage);

            String extension = getFileExtension(targetPath.getFileName().toString());
            boolean success = ImageIO.write(grayscaleImage, extension, targetPath.toFile());

            if (!success) {
                throw new InvalidImageException("Formato de imagem não suportado para escrita: " + extension);
            }
        } catch (IOException e) {
            log.error("Erro ao converter imagem para escala de cinza: {}", sourcePath, e);
            throw e;
        }
    }

    private String getFileExtension(String filename) {
        int lastIndex = filename.lastIndexOf('.');
        if (lastIndex == -1) {
            return "jpg"; // Formato padrão
        }
        return filename.substring(lastIndex + 1);
    }
}