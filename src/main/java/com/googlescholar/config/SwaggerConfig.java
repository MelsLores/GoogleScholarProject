package com.googlescholar.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI 3 Configuration for Google Scholar API
 * 
 * This configuration provides comprehensive API documentation and testing interface
 * for the Google Scholar research paper and author search API.
 * 
 * @author Melany Rivera
 * @date October 2, 2025
 * @version 1.0
 */
@Configuration
public class SwaggerConfig {

    /**
     * Configure OpenAPI documentation for Google Scholar API
     * 
     * @return OpenAPI configuration with complete API information
     */
    @Bean
    public OpenAPI googleScholarOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl("http://localhost:8080");
        devServer.setDescription("Development Server");

        Server prodServer = new Server();
        prodServer.setUrl("https://api.googlescholar.com");
        prodServer.setDescription("Production Server");

        Contact contact = new Contact();
        contact.setEmail("melany.rivera@googlescholar.com");
        contact.setName("Melany Rivera");
        contact.setUrl("https://github.com/MelsLores");

        License mitLicense = new License()
                .name("MIT License")
                .url("https://choosealicense.com/licenses/mit/");

        Info info = new Info()
                .title("Google Scholar API")
                .version("1.0.0")
                .contact(contact)
                .description("Comprehensive API for searching Google Scholar academic papers, authors, and citations. " +
                           "This API provides access to academic research data including author profiles, " +
                           "citation metrics, publication lists, and advanced search capabilities.")
                .termsOfService("https://api.googlescholar.com/terms")
                .license(mitLicense);

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer, prodServer));
    }
}