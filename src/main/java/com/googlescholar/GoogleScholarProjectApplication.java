package com.googlescholar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;

/**
 * Main application class for Google Scholar Project
 * JPA disabled for demo purposes to focus on API functionality
 * 
 * @author Melany Rivera
 * @since October 2, 2025
 */
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class})
public class GoogleScholarProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(GoogleScholarProjectApplication.class, args);
    }
}
