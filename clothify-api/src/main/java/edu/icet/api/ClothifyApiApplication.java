package edu.icet.api;

import edu.icet.db.DatabaseMigrator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ClothifyApiApplication {

    public static void main(String[] args) {
        DatabaseMigrator.migrate();
        SpringApplication.run(ClothifyApiApplication.class, args);
    }
}
