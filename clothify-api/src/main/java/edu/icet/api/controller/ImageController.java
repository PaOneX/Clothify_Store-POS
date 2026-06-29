package edu.icet.api.controller;

import edu.icet.service.ImageStorageService;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    @GetMapping("/**")
    public ResponseEntity<Resource> serveImage(jakarta.servlet.http.HttpServletRequest request) {
        String fullPath = request.getRequestURI().substring("/api/images/".length());
        if (fullPath.isBlank()) {
            return ResponseEntity.notFound().build();
        }

        try {
            Path uploaded = ImageStorageService.getInstance().resolveStoredPath(fullPath);
            if (uploaded != null && Files.isRegularFile(uploaded)) {
                return ResponseEntity.ok()
                        .contentType(guessMediaType(fullPath))
                        .body(new FileSystemResource(uploaded));
            }
        } catch (Exception ignored) {
        }

        String classpathPath = fullPath.startsWith("images/") ? fullPath : "images/" + fullPath;
        ClassPathResource resource = new ClassPathResource(classpathPath);
        if (resource.exists()) {
            return ResponseEntity.ok()
                    .contentType(guessMediaType(classpathPath))
                    .body(resource);
        }

        ClassPathResource placeholder = new ClassPathResource("images/products/placeholder.png");
        return placeholder.exists()
                ? ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(placeholder)
                : ResponseEntity.notFound().build();
    }

    private MediaType guessMediaType(String path) {
        String lower = path.toLowerCase();
        if (lower.endsWith(".png")) return MediaType.IMAGE_PNG;
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return MediaType.IMAGE_JPEG;
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
