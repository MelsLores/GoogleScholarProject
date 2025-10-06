package com.googlescholar.service;

import com.googlescholar.config.SerpApiProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Enhanced Google Scholar Service with pagination, error handling, and validation
 * Addresses the feedback on implementing complete API manipulation
 * 
 * @author GitHub Copilot - Enhanced for Sprint 3
 * @since October 5, 2025
 */
@Service
public class EnhancedScholarService {

    @Autowired
    private ArticleDatabaseService articleDatabaseService;

    @Autowired
    private SerpApiProperties serpApiProperties;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Search with pagination support
     * Implementation requirement: Complete API manipulation with pagination
     */
    public Map<String, Object> searchWithPagination(String query, int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Calculate start position for pagination
            int pageStart = (page - 1) * pageSize;
            
            // Validation
            if (query == null || query.trim().isEmpty()) {
                result.put("error", "Query parameter is required");
                return result;
            }
            
            if (page < 1) {
                result.put("error", "Page number must be 1 or greater");
                return result;
            }
            
            if (pageSize < 1 || pageSize > 100) {
                result.put("error", "Page size must be between 1 and 100");
                return result;
            }
            
            // Construct API URL
            String url = UriComponentsBuilder.fromHttpUrl(serpApiProperties.getBaseUrl())
                .queryParam("engine", "google_scholar")
                .queryParam("q", query)
                .queryParam("start", pageStart)
                .queryParam("num", pageSize)
                .queryParam("api_key", serpApiProperties.getApiKey())
                .build()
                .toUriString();
            
            // Call API
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null) {
                // Save to database automatically - convert Map to JSON string
                String jsonResponse = objectMapper.writeValueAsString(response);
                articleDatabaseService.saveGoogleScholarResults(jsonResponse);
                
                // Build enhanced response
                result.put("query", query);
                result.put("page", page);
                result.put("pageSize", pageSize);
                result.put("pageStart", pageStart);
                result.put("data", response);
                result.put("timestamp", System.currentTimeMillis());
                result.put("success", true);
                
                // Extract organic results for summary
                List<Map<String, Object>> organicResults = (List<Map<String, Object>>) response.get("organic_results");
                if (organicResults != null) {
                    result.put("resultsCount", organicResults.size());
                    result.put("hasResults", true);
                } else {
                    result.put("resultsCount", 0);
                    result.put("hasResults", false);
                }
                
            } else {
                result.put("error", "No response from API");
                result.put("success", false);
            }
            
        } catch (Exception e) {
            result.put("error", "API call failed: " + e.getMessage());
            result.put("success", false);
            e.printStackTrace();
        }
        
        return result;
    }

    /**
     * Get search suggestions based on partial query
     * Implementation requirement: Enhanced user experience
     */
    public Map<String, Object> getSearchSuggestions(String partialQuery) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Common academic search terms that might be relevant
            List<String> suggestions = new ArrayList<>();
            
            if (partialQuery != null && !partialQuery.trim().isEmpty()) {
                String query = partialQuery.toLowerCase().trim();
                
                // Academic disciplines
                if (query.contains("machine") || query.contains("ml")) {
                    suggestions.add("machine learning");
                    suggestions.add("machine learning algorithms");
                    suggestions.add("deep learning");
                }
                
                if (query.contains("data")) {
                    suggestions.add("data science");
                    suggestions.add("data mining");
                    suggestions.add("big data");
                }
                
                if (query.contains("ai") || query.contains("artificial")) {
                    suggestions.add("artificial intelligence");
                    suggestions.add("AI ethics");
                    suggestions.add("neural networks");
                }
                
                if (query.contains("software")) {
                    suggestions.add("software engineering");
                    suggestions.add("software architecture");
                    suggestions.add("software testing");
                }
                
                // If no specific suggestions, provide general academic terms
                if (suggestions.isEmpty()) {
                    suggestions.add(partialQuery + " research");
                    suggestions.add(partialQuery + " methodology");
                    suggestions.add(partialQuery + " analysis");
                }
            }
            
            result.put("suggestions", suggestions);
            result.put("query", partialQuery);
            result.put("success", true);
            
        } catch (Exception e) {
            result.put("error", "Failed to generate suggestions: " + e.getMessage());
            result.put("success", false);
        }
        
        return result;
    }

    /**
     * Validate API connectivity and configuration
     * Implementation requirement: System health monitoring
     */
    public Map<String, Object> validateApiConnectivity() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Check configuration
            if (serpApiProperties.getApiKey() == null || serpApiProperties.getApiKey().trim().isEmpty()) {
                result.put("error", "API key is not configured");
                result.put("configValid", false);
                return result;
            }
            
            // Test simple API call
            String testQuery = "test";
            String testUrl = UriComponentsBuilder.fromHttpUrl(serpApiProperties.getBaseUrl())
                .queryParam("engine", "google_scholar")
                .queryParam("q", testQuery)
                .queryParam("num", 1)
                .queryParam("api_key", serpApiProperties.getApiKey())
                .build()
                .toUriString();
            
            Map<String, Object> response = restTemplate.getForObject(testUrl, Map.class);
            
            if (response != null && !response.containsKey("error")) {
                result.put("connectivity", "SUCCESS");
                result.put("configValid", true);
                result.put("apiKey", serpApiProperties.getApiKey().substring(0, 8) + "...");
                result.put("baseUrl", serpApiProperties.getBaseUrl());
                result.put("testQuery", testQuery);
                result.put("success", true);
            } else {
                result.put("connectivity", "FAILED");
                result.put("error", response != null ? response.get("error") : "Unknown API error");
                result.put("success", false);
            }
            
        } catch (Exception e) {
            result.put("connectivity", "ERROR");
            result.put("error", "Connection test failed: " + e.getMessage());
            result.put("success", false);
        }
        
        return result;
    }

    /**
     * Get service statistics and health information
     * Implementation requirement: System monitoring
     */
    public Map<String, Object> getServiceHealth() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            result.put("serviceName", "EnhancedScholarService");
            result.put("version", "1.0.0");
            result.put("timestamp", System.currentTimeMillis());
            result.put("uptime", System.currentTimeMillis()); // Simple uptime approximation
            
            // Configuration status
            Map<String, Object> config = new HashMap<>();
            config.put("hasApiKey", serpApiProperties.getApiKey() != null && !serpApiProperties.getApiKey().trim().isEmpty());
            config.put("baseUrl", serpApiProperties.getBaseUrl());
            config.put("timeoutMs", serpApiProperties.getTimeoutMs());
            config.put("maxRetries", serpApiProperties.getMaxRetries());
            result.put("configuration", config);
            
            // Database connectivity check
            try {
                Map<String, Object> dbHealth = articleDatabaseService.validateDataIntegrity();
                result.put("databaseConnectivity", dbHealth.get("success"));
            } catch (Exception e) {
                result.put("databaseConnectivity", false);
                result.put("databaseError", e.getMessage());
            }
            
            result.put("status", "HEALTHY");
            result.put("success", true);
            
        } catch (Exception e) {
            result.put("status", "UNHEALTHY");
            result.put("error", e.getMessage());
            result.put("success", false);
        }
        
        return result;
    }
}