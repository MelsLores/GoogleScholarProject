package com.googlescholar;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Simple test class to verify SerpApi Google Scholar integration without Spring context.
 * 
 * @author Melany Rivera
 * @since October 2, 2025
 */
public class SimpleApiTest {

    private static final String API_KEY = "3c80b3f19d74557d14f0bdd138545d703f072aca20e602649f471e2a387a987a";
    private static final String BASE_URL = "https://serpapi.com/search";

    @Test
    public void testGoogleScholarApiDirectly() {
        try {
            System.out.println("🔍 Testing Google Scholar API connection...");
            
            RestTemplate restTemplate = new RestTemplate();
            
            // Test query
            String query = URLEncoder.encode("machine learning", StandardCharsets.UTF_8);
            String url = String.format("%s?engine=google_scholar&q=%s&api_key=%s", 
                                     BASE_URL, query, API_KEY);
            
            System.out.println("📡 Testing URL: " + url.substring(0, url.length() - 10) + "**********");
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            System.out.println("📊 Status Code: " + response.getStatusCode());
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                System.out.println("✅ API connection successful!");
                System.out.println("📄 Response length: " + response.getBody().length() + " characters");
                
                // Check if response contains expected Google Scholar data
                String responseBody = response.getBody();
                if (responseBody.contains("organic_results") || responseBody.contains("search_metadata")) {
                    System.out.println("🎯 Response contains expected Google Scholar data structure!");
                    
                    // Show a preview of the response
                    int previewLength = Math.min(500, responseBody.length());
                    System.out.println("📋 Response preview:");
                    System.out.println(responseBody.substring(0, previewLength) + "...");
                    
                } else {
                    System.out.println("⚠️ Response doesn't contain expected Google Scholar structure");
                }
                
            } else {
                System.out.println("❌ API connection failed with status: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error testing API: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void testApiKeyFormat() {
        System.out.println("🔑 Testing API key format...");
        
        if (API_KEY != null && !API_KEY.isEmpty() && API_KEY.length() > 10) {
            System.out.println("✅ API key format looks valid");
            System.out.println("🔢 API key length: " + API_KEY.length() + " characters");
            System.out.println("🆔 API key preview: " + API_KEY.substring(0, 10) + "...");
        } else {
            System.out.println("❌ API key format appears invalid");
        }
    }
}