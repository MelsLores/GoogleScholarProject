package com.googlescholar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.googlescholar.config.SerpApiProperties;

/**
 * Main application class for Google Scholar Project
 * Enhanced for Sprint 3 with configuration properties support
 * JPA temporarily disabled to test basic database connectivity
 * 
 * @author Melany Rivera - Enhanced by GitHub Copilot
 * @since October 2, 2025
 */
@SpringBootApplication(exclude = {
    HibernateJpaAutoConfiguration.class,
    JpaRepositoriesAutoConfiguration.class
})
@EnableConfigurationProperties(SerpApiProperties.class)
public class GoogleScholarProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(GoogleScholarProjectApplication.class, args);
    }
}
