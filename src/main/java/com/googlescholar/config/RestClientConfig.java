package com.googlescholar.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Configuration class for HTTP client setup
 * Configures Apache HttpClient with connection pooling and timeouts
 * 
 * @author Melany Rivera
 * @since October 2, 2025
 */
@Configuration
public class RestClientConfig {

    /**
     * Creates an Apache HttpClient bean for HTTP requests
     * Configured with connection pooling and proper timeouts
     * 
     * @return CloseableHttpClient instance with optimized configuration
     * @author Melany Rivera
     * @since October 2, 2025
     */
    @Bean
    public CloseableHttpClient httpClient() {
        HttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(100)
                .setMaxConnPerRoute(20)
                .build();

        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setConnectionManagerShared(false)
                .evictExpiredConnections()
                .evictIdleConnections(Timeout.ofSeconds(30))
                .build();
    }

    /**
     * Creates an ObjectMapper bean for JSON processing
     * 
     * @return ObjectMapper instance for JSON serialization/deserialization
     * @author Melany Rivera
     * @since October 2, 2025
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}