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
 * Test to diagnose the problem with null fields in the response
 * 
 * @author Melany Rivera
 * @since October 2, 2025
 */
public class ResponseDiagnosticTest {

    private static final String API_KEY = "3c80b3f19d74557d14f0bdd138545d703f072aca20e602649f471e2a387a987a";
    private static final String BASE_URL = "https://serpapi.com/search";

    @Test
    public void diagnoseResponseIssue() {
        try {
            System.out.println("🔍 DIAGNÓSTICO DE RESPUESTA");
            System.out.println("============================\n");

            RestTemplate restTemplate = new RestTemplate();
            ObjectMapper mapper = new ObjectMapper();
            
            // Test con parámetros específicos
            String query = URLEncoder.encode("machine learning", StandardCharsets.UTF_8);
            String url = String.format("%s?engine=google_scholar&q=%s&api_key=%s&num=5&hl=en", 
                                     BASE_URL, query, API_KEY);
            
            System.out.println("📡 URL de prueba:");
            System.out.println(url.substring(0, url.length() - 20) + "...[API_KEY]");
            System.out.println();
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                String responseBody = response.getBody();
                System.out.println("✅ Status: " + response.getStatusCode());
                System.out.println("📄 Response Length: " + responseBody.length() + " characters");
                
                // Parse JSON
                JsonNode jsonResponse = mapper.readTree(responseBody);
                
                // Verificar cada campo
                System.out.println("\n🔍 ANÁLISIS DE CAMPOS:");
                
                if (jsonResponse.has("search_metadata")) {
                    JsonNode metadata = jsonResponse.get("search_metadata");
                    System.out.println("✅ search_metadata: PRESENTE");
                    if (metadata.has("status")) {
                        System.out.println("   └─ Status: " + metadata.get("status").asText());
                    }
                } else {
                    System.out.println("❌ search_metadata: AUSENTE");
                }
                
                if (jsonResponse.has("search_parameters")) {
                    System.out.println("✅ search_parameters: PRESENTE");
                    JsonNode params = jsonResponse.get("search_parameters");
                    if (params.has("q")) {
                        System.out.println("   └─ Query: " + params.get("q").asText());
                    }
                } else {
                    System.out.println("❌ search_parameters: AUSENTE");
                }
                
                if (jsonResponse.has("search_information")) {
                    System.out.println("✅ search_information: PRESENTE");
                } else {
                    System.out.println("❌ search_information: AUSENTE");
                }
                
                if (jsonResponse.has("organic_results")) {
                    JsonNode results = jsonResponse.get("organic_results");
                    System.out.println("✅ organic_results: PRESENTE");
                    System.out.println("   └─ Número de resultados: " + results.size());
                    
                    if (results.size() > 0) {
                        JsonNode firstResult = results.get(0);
                        System.out.println("   └─ Primer resultado:");
                        if (firstResult.has("title")) {
                            System.out.println("      └─ Título: " + firstResult.get("title").asText());
                        }
                        if (firstResult.has("link")) {
                            System.out.println("      └─ Link: " + firstResult.get("link").asText());
                        }
                    }
                } else {
                    System.out.println("❌ organic_results: AUSENTE");
                }
                
                if (jsonResponse.has("pagination")) {
                    JsonNode pagination = jsonResponse.get("pagination");
                    System.out.println("✅ pagination: PRESENTE");
                    if (pagination.has("current")) {
                        System.out.println("   └─ Página actual: " + pagination.get("current").asInt());
                    }
                    if (pagination.has("next")) {
                        System.out.println("   └─ Siguiente página: " + pagination.get("next").asText());
                    }
                } else {
                    System.out.println("❌ pagination: AUSENTE");
                }
                
                // Mostrar respuesta raw (primeros 1000 caracteres)
                System.out.println("\n📋 RESPUESTA RAW (primeros 1000 caracteres):");
                System.out.println(responseBody.substring(0, Math.min(1000, responseBody.length())));
                if (responseBody.length() > 1000) {
                    System.out.println("\n... [contenido truncado] ...");
                }
                
                // Verificar si hay errores en la respuesta
                if (jsonResponse.has("error")) {
                    System.out.println("\n❌ ERROR EN RESPUESTA:");
                    System.out.println(jsonResponse.get("error").toString());
                }
                
            } else {
                System.out.println("❌ Error HTTP: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            System.out.println("❌ Error en diagnóstico: " + e.getMessage());
            e.printStackTrace();
        }
    }
}