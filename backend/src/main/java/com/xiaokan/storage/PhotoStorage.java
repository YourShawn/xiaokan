package com.xiaokan.storage;

import com.xiaokan.config.XiaokanProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
public class PhotoStorage {

    private final Path root;

    public PhotoStorage(XiaokanProperties properties) throws IOException {
        this.root = Path.of(properties.getUploadDir()).toAbsolutePath().normalize();
        Files.createDirectories(this.root);
    }

    public StoredPhoto save(Long scanId, int index, MultipartFile file) throws IOException {
        String ext = extension(file.getOriginalFilename(), file.getContentType());
        String filename = scanId + "-" + index + "-" + UUID.randomUUID() + ext;
        Path dest = root.resolve(filename);
        Files.copy(file.getInputStream(), dest);
        return new StoredPhoto(filename, dest.toString(), file.getSize());
    }

    public Path resolve(String storagePath) {
        Path path = Path.of(storagePath);
        if (!path.isAbsolute()) {
            path = root.resolve(path);
        }
        return path.normalize();
    }

    private static String extension(String original, String contentType) {
        if (original != null && original.contains(".")) {
            String ext = original.substring(original.lastIndexOf('.')).toLowerCase();
            if (ext.matches("\\.(jpg|jpeg|png|webp|gif|heic)")) {
                return ext;
            }
        }
        if (contentType != null) {
            return switch (contentType) {
                case "image/png" -> ".png";
                case "image/webp" -> ".webp";
                case "image/gif" -> ".gif";
                default -> ".jpg";
            };
        }
        return ".jpg";
    }

    public record StoredPhoto(String filename, String absolutePath, long sizeBytes) {
    }
}
