package edu.icet.util;

import edu.icet.config.AppConfig;
import edu.icet.service.ImageStorageService;
import javafx.scene.image.Image;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ImageUtil {

    private static final String PLACEHOLDER = "images/products/placeholder.png";
    private static final int DEFAULT_WIDTH = 200;
    private static final int DEFAULT_HEIGHT = 200;

    private ImageUtil() {
    }

    public static Image getProductImage(String imagePath) {
        return getProductImage(imagePath, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public static Image getProductImage(String imagePath, double width, double height) {
        if (imagePath != null && !imagePath.isBlank()) {
            Image image = loadFromClasspath(imagePath, width, height);
            if (image != null) {
                return image;
            }
            image = loadFromUploads(imagePath, width, height);
            if (image != null) {
                return image;
            }
            image = loadFromFile(imagePath, width, height);
            if (image != null) {
                return image;
            }
        }
        return loadFromClasspath(PLACEHOLDER, width, height);
    }

    public static Image getLogo() {
        Image logo = loadFromClasspath("images/logo.png", 200, 200);
        return logo != null ? logo : loadFromClasspath(PLACEHOLDER, 200, 200);
    }

    public static Image getIcon(String name) {
        return loadFromClasspath("images/icons/" + name, 24, 24);
    }

    private static Image loadFromClasspath(String path, double width, double height) {
        String normalized = normalizePath(path);
        for (String candidate : new String[]{normalized, "/" + normalized}) {
            try (InputStream in = ImageUtil.class.getResourceAsStream(candidate)) {
                if (in != null) {
                    return new Image(in, width, height, true, true);
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private static Image loadFromUploads(String relativePath, double width, double height) {
        try {
            Path stored = ImageStorageService.getInstance().resolveStoredPath(relativePath);
            if (stored != null && Files.isRegularFile(stored)) {
                try (InputStream in = Files.newInputStream(stored)) {
                    return new Image(in, width, height, true, true);
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static Image loadFromFile(String path, double width, double height) {
        try (InputStream in = new FileInputStream(path)) {
            return new Image(in, width, height, true, true);
        } catch (Exception ignored) {
        }
        return null;
    }

    private static String normalizePath(String path) {
        String normalized = path.trim().replace('\\', '/');
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }
}
