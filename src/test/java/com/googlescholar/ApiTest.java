package com.googlescholar;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Test class to verify SerpApi Google Scholar integration.
 * 
 * @author Melany Rivera
 * @since October 2, 2025
 */
@SpringBootTest
public class ApiTest {

    private static final String API_KEY = "3c80b3f19d74557d14f0bdd138545d703f072aca20e602649f471e2a387a987a";
    private static final String BASE_URL = "https://serpapi.com/search";

    @Test
    public void testGoogleScholarApiConnection() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            
            // Test query
            String query = URLEncoder.encode("machine learning", StandardCharsets.UTF_8);
            String url = String.format("%s?engine=google_scholar&q=%s&api_key=%s", 
                                     BASE_URL, query, API_KEY);
            
            System.out.println("Testing URL: " + url);
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            System.out.println("Status Code: " + response.getStatusCode());
            System.out.println("Response Body Preview: " + 
                             response.getBody().substring(0, Math.min(500, response.getBody().length())));
            
            if (response.getStatusCode() == HttpStatus.OK) {
                System.out.println("✅ API connection successful!");
            } else {
                System.out.println("❌ API connection failed with status: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error testing API: " + e.getMessage());
            e.printStackTrace();
        }
    }
}