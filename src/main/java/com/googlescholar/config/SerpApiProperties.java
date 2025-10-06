package com.googlescholar.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for SerpApi integration
 * Addresses feedback: Unknown property warnings and better configuration management
 * 
 * @author GitHub Copilot - Enhanced for Sprint 3
 * @since October 5, 2025
 */
@Component
@ConfigurationProperties(prefix = "serpapi")
public class SerpApiProperties {
    
    /**
     * SerpApi API key for authentication
     */
    private String apiKey;
    
    /**
     * Base URL for SerpApi requests
     */
    private String baseUrl = "https://serpapi.com/search";
    
    /**
     * Request timeout in milliseconds
     */
    private int timeoutMs = 10000;
    
    /**
     * Maximum retries for failed requests
     */
    private int maxRetries = 3;
    
    /**
     * Rate limit delay between requests in milliseconds
     */
    private int rateLimitDelayMs = 1000;
    
    // Getters and setters
    public String getApiKey() {
        return apiKey;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    public int getTimeoutMs() {
        return timeoutMs;
    }
    
    public void setTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }
    
    public int getMaxRetries() {
        return maxRetries;
    }
    
    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }
    
    public int getRateLimitDelayMs() {
        return rateLimitDelayMs;
    }
    
    public void setRateLimitDelayMs(int rateLimitDelayMs) {
        this.rateLimitDelayMs = rateLimitDelayMs;
    }
    
    /**
     * Validate configuration
     */
    public boolean isValid() {
        return apiKey != null && !apiKey.trim().isEmpty() && 
               baseUrl != null && !baseUrl.trim().isEmpty();
    }
}