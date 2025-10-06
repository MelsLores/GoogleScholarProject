package com.googlescholar.example;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.Map;

/**
 * Example class demonstrating how to use the Google Scholar API with database integration
 * This shows how to get articles from the Author Articles API and save them to SQL Server
 * 
 * @author Melany Rivera
 * @since October 2, 2025
 */
@Component
public class GoogleScholarApiExample {
    
    private final String baseUrl = "http://localhost:8080/api/v1/scholar";
    
    /**
     * Example of how to fetch articles from Google Scholar Author Articles API
     * and save them to the database using your application
     */
    public void demonstrateApiUsage() {
        // Example JSON response from Google Scholar Author Articles API
        String exampleApiResponse = """
            {
              "articles": [
                {
                  "title": "Lifetime prevalence and age-of-onset distributions of DSM-IV disorders in the National Comorbidity Survey Replication",
                  "link": "https://scholar.google.com/citations?view_op=view_citation&hl=en&user=EicYvbwAAAAJ&citation_for_view=EicYvbwAAAAJ:UeHWp8X0CEIC",
                  "citation_id": "EicYvbwAAAAJ:UeHWp8X0CEIC",
                  "authors": "RC Kessler, P Berglund, O Demler, R Jin, KR Merikangas, EE Walters",
                  "publication": "Archives of general psychiatry 62 (6), 593-602, 2005",
                  "cited_by": {
                    "value": 29693,
                    "link": "https://scholar.google.com/scholar?oi=bibs&hl=en&cites=2173726401600747709",
                    "serpapi_link": "https://serpapi.com/search.json?cites=2173726401600747709&engine=google_scholar&hl=en",
                    "cites_id": "2173726401600747709"
                  },
                  "year": "2005"
                },
                {
                  "title": "Lifetime and 12-month prevalence of DSM-III-R psychiatric disorders in the United States",
                  "link": "https://scholar.google.com/citations?view_op=view_citation&hl=en&user=EicYvbwAAAAJ&citation_for_view=EicYvbwAAAAJ:u5HHmVD_uO8C",
                  "citation_id": "EicYvbwAAAAJ:u5HHmVD_uO8C",
                  "authors": "RC Kessler, KA McGonagle, S Zhao, CB Nelson, M Hughes, S Eshleman",
                  "publication": "Archives of general psychiatry 51 (1), 8-19, 1994",
                  "cited_by": {
                    "value": 18077,
                    "link": "https://scholar.google.com/scholar?oi=bibs&hl=en&cites=9166229658659884654",
                    "serpapi_link": "https://serpapi.com/search.json?cites=9166229658659884654&engine=google_scholar&hl=en",
                    "cites_id": "9166229658659884654"
                  },
                  "year": "1994"
                }
              ]
            }
            """;
        
        try {
            RestTemplate restTemplate = new RestTemplate();
            
            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Create request entity
            HttpEntity<String> requestEntity = new HttpEntity<>(exampleApiResponse, headers);
            
            // Make POST request to save articles
            ResponseEntity<Map> response = restTemplate.postForEntity(
                baseUrl + "/save-articles", 
                requestEntity, 
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                System.out.println("✅ Articles saved successfully!");
                System.out.println("📊 Response: " + responseBody);
            } else {
                System.out.println("❌ Error saving articles: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            System.out.println("❌ Exception occurred: " + e.getMessage());
        }
    }
    
    /**
     * Example of how to test database connection
     */
    public void testDatabaseConnection() {
        try {
            RestTemplate restTemplate = new RestTemplate();
            
            ResponseEntity<Map> response = restTemplate.getForEntity(
                baseUrl + "/database/test", 
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> responseBody = response.getBody();
                boolean connected = (Boolean) responseBody.get("connected");
                
                if (connected) {
                    System.out.println("✅ Database connection successful!");
                } else {
                    System.out.println("❌ Database connection failed!");
                }
                
                System.out.println("📊 Response: " + responseBody);
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error testing database connection: " + e.getMessage());
        }
    }
    
    /**
     * Java code example for calling SerpApi Google Scholar Author Articles API
     * This is the code you would use to get the JSON response to save
     */
    public void serpApiUsageExample() {
        System.out.println("""
            
            // Example Java code for SerpApi Google Scholar Author Articles API:
            
            import java.util.Map;
            import java.util.HashMap;
            import serpapi.GoogleSearch;
            import serpapi.SerpApiSearchException;
            import com.google.gson.JsonObject;
            
            Map<String, String> parameter = new HashMap<>();
            parameter.put("engine", "google_scholar_author");
            parameter.put("author_id", "EicYvbwAAAAJ");  // Example author ID
            parameter.put("api_key", "your_serpapi_key_here");
            
            GoogleSearch search = new GoogleSearch(parameter);
            
            try {
                JsonObject results = search.getJson();
                String jsonResponse = results.toString();
                
                // Now use your application to save the articles:
                // POST to http://localhost:8080/api/v1/scholar/save-articles
                // with the jsonResponse as the request body
                
            } catch (SerpApiSearchException ex) {
                System.out.println("Exception: " + ex.toString());
            }
            """);
    }
}