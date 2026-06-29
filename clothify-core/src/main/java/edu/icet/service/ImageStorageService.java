package edu.icet.service;

import edu.icet.config.AppConfig;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

public final class ImageStorageService {

    private static final long MAX_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXT = Set.of("png", "jpg", "jpeg");

    private static ImageStorageService instance;

    private ImageStorageService() {
    }

    public static ImageStorageService getInstance() {
        if (instance == null) {
            instance = new ImageStorageService();
        }
        return instance;
    }

    public Path getUploadsRoot() {
        return Path.of(AppConfig.getUploadsDir()).toAbsolutePath().normalize();
    }

    public Path resolveStoredPath(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return null;
        }
        Path resolved = getUploadsRoot().resolve(relativePath).normalize();
        if (!resolved.startsWith(getUploadsRoot())) {
            throw new IllegalArgumentException("Invalid image path");
        }
        return resolved;
    }

    public String saveProductImage(java.io.File source, int productId) throws IOException {
        if (source == null || !source.isFile()) {
            throw new IllegalArgumentException("Image file not found");
        }
        if (source.length() > MAX_BYTES) {
            throw new IllegalArgumentException("Image must be 5 MB or smaller");
        }
        String ext = extensionOf(source.getName());
        if (!ALLOWED_EXT.contains(ext)) {
            throw new IllegalArgumentException("Only PNG and JPEG images are allowed");
        }

        Path productDir = getUploadsRoot().resolve("products").resolve(String.valueOf(productId));
        Files.createDirectories(productDir);

        String fileName = UUID.randomUUID() + "." + ext;
        Path target = productDir.resolve(fileName);
        try (InputStream in = Files.newInputStream(source.toPath())) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return "products/" + productId + "/" + fileName;
    }

    public void deleteStoredImage(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return;
        }
        try {
            Path path = resolveStoredPath(relativePath);
            if (path != null) {
                Files.deleteIfExists(path);
            }
        } catch (Exception ignored) {
        }
    }

    public boolean isPendingLocalFile(String path) {
        if (path == null || path.isBlank()) {
            return false;
        }
        if (path.startsWith("products/") || path.startsWith("images/")) {
            return false;
        }
        return Files.isRegularFile(Path.of(path));
    }

    private static String extensionOf(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            throw new IllegalArgumentException("Image file must have an extension");
        }
        return fileName.substring(dot + 1).toLowerCase();
    }
}
