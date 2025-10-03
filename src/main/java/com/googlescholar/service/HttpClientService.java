package com.googlescholar.service;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.ParseException;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * HTTP service using Apache HttpClient for Google Scholar API calls
 * Provides high-performance HTTP communication with proper resource management
 * 
 * @author Melany Rivera
 * @since October 2, 2025
 */
@Service
public class HttpClientService {

    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;

    /**
     * Constructor with dependency injection
     * 
     * @param httpClient the Apache HttpClient instance
     * @param objectMapper the JSON object mapper
     */
    public HttpClientService(CloseableHttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Executes a GET request and returns the response as a String
     * 
     * @param url the URL to make the GET request to
     * @return the response body as a String
     * @throws IOException if an error occurs during the HTTP request
     * @throws ParseException if an error occurs parsing the response
     */
    public String executeGetRequest(String url) throws IOException, ParseException {
        HttpGet httpGet = new HttpGet(url);
        
        // Set headers for better API compatibility
        httpGet.setHeader("User-Agent", "GoogleScholarAPI/1.0");
        httpGet.setHeader("Accept", "application/json");
        
        return httpClient.execute(httpGet, response -> {
            HttpEntity entity = response.getEntity();
            
            if (entity != null) {
                String responseBody = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                EntityUtils.consume(entity); // Ensure the entity is consumed
                return responseBody;
            }
            
            return null;
        });
    }

    /**
     * Executes a GET request and parses the response to a specific type
     * 
     * @param url the URL to make the GET request to
     * @param responseType the class type to parse the response to
     * @param <T> the type parameter
     * @return the parsed response object
     * @throws IOException if an error occurs during the HTTP request or parsing
     * @throws ParseException if an error occurs parsing the HTTP response
     */
    public <T> T executeGetRequest(String url, Class<T> responseType) throws IOException, ParseException {
        String responseBody = executeGetRequest(url);
        
        if (responseBody != null) {
            return objectMapper.readValue(responseBody, responseType);
        }
        
        return null;
    }

    /**
     * Checks if the HTTP client is healthy and can make requests
     * 
     * @return true if the client is healthy, false otherwise
     */
    public boolean isHealthy() {
        try {
            // Test with a simple request to verify connectivity
            executeGetRequest("https://httpbin.org/status/200");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}