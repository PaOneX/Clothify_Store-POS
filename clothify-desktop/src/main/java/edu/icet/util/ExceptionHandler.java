package edu.icet.util;

import edu.icet.exception.AppException;
import javafx.scene.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ExceptionHandler.class);

    private ExceptionHandler() {
    }

    public static void handle(Node owner, String title, Exception e) {
        Throwable cause = e;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        log.error("{}: {}", title, cause.getMessage(), e);
        String message = cause instanceof AppException || cause instanceof IllegalArgumentException
                || cause instanceof IllegalStateException
                ? cause.getMessage()
                : "An unexpected error occurred. Please try again.";
        AlertUtil.showError(title, message);
    }
}
