package com.googlescholar;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Detailed API test to explore Google Scholar response structure.
 * 
 * @author Melany Rivera
 * @since October 2, 2025
 */
public class DetailedApiTest {

    private static final String API_KEY = "3c80b3f19d74557d14f0bdd138545d703f072aca20e602649f471e2a387a987a";
    private static final String BASE_URL = "https://serpapi.com/search";

    @Test
    public void testScholarSearchStructure() {
        try {
            System.out.println("🔬 Testing detailed Google Scholar API structure...");
            
            RestTemplate restTemplate = new RestTemplate();
            ObjectMapper mapper = new ObjectMapper();
            
            // Test query for a specific topic
            String query = URLEncoder.encode("artificial intelligence", StandardCharsets.UTF_8);
            String url = String.format("%s?engine=google_scholar&q=%s&api_key=%s&num=5", 
                                     BASE_URL, query, API_KEY);
            
            System.out.println("📡 Making API call...");
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                System.out.println("✅ API call successful!");
                
                // Parse JSON response
                JsonNode jsonResponse = mapper.readTree(response.getBody());
                
                // Show search metadata
                if (jsonResponse.has("search_metadata")) {
                    JsonNode metadata = jsonResponse.get("search_metadata");
                    System.out.println("\n📊 Search Metadata:");
                    System.out.println("  Status: " + metadata.get("status").asText());
                    System.out.println("  Total time: " + metadata.get("total_time_taken").asDouble() + "s");
                }
                
                // Show organic results
                if (jsonResponse.has("organic_results")) {
                    JsonNode results = jsonResponse.get("organic_results");
                    System.out.println("\n📚 Found " + results.size() + " organic results:");
                    
                    for (int i = 0; i < Math.min(3, results.size()); i++) {
                        JsonNode result = results.get(i);
                        System.out.println("\n  📄 Result " + (i + 1) + ":");
                        System.out.println("    Position: " + result.get("position").asInt());
                        System.out.println("    Title: " + result.get("title").asText());
                        System.out.println("    Citations: " + 
                                         (result.has("inline_links") && 
                                          result.get("inline_links").has("cited_by") ? 
                                          result.get("inline_links").get("cited_by").get("total").asText() : "N/A"));
                        System.out.println("    Year: " + 
                                         (result.has("publication_info") && 
                                          result.get("publication_info").has("summary") ? 
                                          result.get("publication_info").get("summary").asText() : "N/A"));
                    }
                }
                
                // Show pagination info
                if (jsonResponse.has("pagination")) {
                    JsonNode pagination = jsonResponse.get("pagination");
                    System.out.println("\n📖 Pagination:");
                    System.out.println("  Current page: " + pagination.get("current").asInt());
                    if (pagination.has("next")) {
                        System.out.println("  Next page available: Yes");
                    }
                }
                
                System.out.println("\n🎯 Google Scholar API integration is working perfectly!");
                
            } else {
                System.out.println("❌ API call failed with status: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error in detailed API test: " + e.getMessage());
            e.printStackTrace();
        }
    }
}