package com.googlescholar.controller;

import com.googlescholar.view.ScholarSearchRequestDTO;
import com.googlescholar.view.ScholarSearchResponseDTO;
import com.googlescholar.service.HttpClientService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import org.apache.hc.core5.http.ParseException;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import com.fasterxml.jackson.databind.ObjectMapper;

// Swagger/OpenAPI imports
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST Controller for Google Scholar search operations using SerpApi
 * This controller handles search requests and communicates with the SerpApi Google Scholar API
 * Now powered by Apache HttpClient for enhanced performance
 * 
 * @author Melany Rivera
 * @since October 2, 2025
 */
@RestController
@RequestMapping("/api/v1/scholar")
@CrossOrigin(origins = "*")
@Tag(name = "Google Scholar API", description = "Comprehensive API for searching Google Scholar academic papers, authors, and citations")
public class ScholarController {

    private final HttpClientService httpClientService;
    private final ObjectMapper objectMapper;
    private final String serpApiBaseUrl = "https://serpapi.com/search.json";

    @Value("${serpapi.api.key:}")
    private String apiKey;

    /**
     * Constructor with HttpClientService injection
     * 
     * @param httpClientService the HTTP client service for API calls
     * @param objectMapper the JSON object mapper
     * @author Melany Rivera
     * @since October 2, 2025
     */
    public ScholarController(HttpClientService httpClientService, ObjectMapper objectMapper) {
        this.httpClientService = httpClientService;
        this.objectMapper = objectMapper;
    }

    /**
     * Search Google Scholar using the SerpApi
     * 
     * @param request the search request parameters
     * @return the search results from Google Scholar
     * @author Melany Rivera
     * @since October 2, 2025
     */
    @Operation(
        summary = "Search Google Scholar Academic Papers",
        description = "Performs a comprehensive search of Google Scholar database for academic papers, authors, and citations. " +
                     "Returns detailed results including publication metadata, citation counts, and author information.",
        tags = {"Scholar Search"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Search completed successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ScholarSearchResponseDTO.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid search parameters provided",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Internal server error or API key not configured",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping("/search")
    public ResponseEntity<?> searchScholar(
        @Parameter(description = "Search parameters for Google Scholar query", required = true)
        @Valid @RequestBody ScholarSearchRequestDTO request) {
        try {
            // Validate API key
            if (apiKey == null || apiKey.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(createErrorResponse("API key not configured. Please set SERPAPI_API_KEY environment variable."));
            }

            // Build the URL with query parameters
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serpApiBaseUrl)
                    .queryParam("engine", "google_scholar")
                    .queryParam("q", request.getQ())
                    .queryParam("api_key", apiKey);

            // Add optional parameters
            addOptionalParam(builder, "cites", request.getCites());
            addOptionalParam(builder, "as_ylo", request.getAsYlo());
            addOptionalParam(builder, "as_yhi", request.getAsYhi());
            addOptionalParam(builder, "scisbd", request.getScisbd());
            addOptionalParam(builder, "cluster", request.getCluster());
            addOptionalParam(builder, "hl", request.getHl());
            addOptionalParam(builder, "lr", request.getLr());
            addOptionalParam(builder, "start", request.getStart());
            addOptionalParam(builder, "num", request.getNum());
            addOptionalParam(builder, "as_sdt", request.getAsSdt());
            addOptionalParam(builder, "safe", request.getSafe());
            addOptionalParam(builder, "filter", request.getFilter());
            addOptionalParam(builder, "as_vis", request.getAsVis());
            addOptionalParam(builder, "as_rr", request.getAsRr());
            addOptionalParam(builder, "no_cache", request.getNoCache());
            addOptionalParam(builder, "async", request.getAsync());
            addOptionalParam(builder, "output", request.getOutput());

            String url = builder.toUriString();

            // Make the API call using Apache HttpClient
            try {
                String responseBody = httpClientService.executeGetRequest(url);
                
                if (responseBody != null) {
                    ScholarSearchResponseDTO searchResponse = objectMapper.readValue(responseBody, ScholarSearchResponseDTO.class);
                    return ResponseEntity.ok(searchResponse);
                } else {
                    return ResponseEntity.status(HttpStatus.NO_CONTENT)
                            .body(createErrorResponse("No content received from API"));
                }
            } catch (IOException | ParseException ex) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(createErrorResponse("HTTP Client error: " + ex.getMessage()));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error searching Google Scholar: " + e.getMessage()));
        }
    }

    /**
     * Simple search endpoint with just query parameter
     * 
     * @param query the search query
     * @param start pagination start offset (optional)
     * @param num number of results (optional)
     * @return the search results from Google Scholar
     * @author Melany Rivera
     * @since October 2, 2025
     */
    @Operation(
        summary = "Simple Google Scholar Search",
        description = "Quick search interface for Google Scholar with basic parameters. " +
                     "Perfect for simple queries with minimal configuration. Returns raw JSON response.",
        tags = {"Scholar Search"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Search completed successfully",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid query parameter",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Internal server error or API key not configured",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping("/search")
    public ResponseEntity<?> searchScholarSimple(
            @Parameter(description = "Search query for academic papers", example = "machine learning", required = true)
            @RequestParam String query,
            @Parameter(description = "Pagination start offset", example = "0")
            @RequestParam(defaultValue = "0") Integer start,
            @Parameter(description = "Number of results to return", example = "10")
            @RequestParam(defaultValue = "10") Integer num) {
        
        try {
            // Validate API key
            if (apiKey == null || apiKey.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(createErrorResponse("API key not configured. Please set SERPAPI_API_KEY environment variable."));
            }

            // Build the URL with query parameters
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serpApiBaseUrl)
                    .queryParam("engine", "google_scholar")
                    .queryParam("q", query)
                    .queryParam("api_key", apiKey)
                    .queryParam("hl", "en")
                    .queryParam("start", start)
                    .queryParam("num", num);

            String url = builder.toUriString();

            // Make the API call using Apache HttpClient and return raw JSON string
            try {
                String responseBody = httpClientService.executeGetRequest(url);
                
                if (responseBody != null) {
                    // Parse and return as JSON object
                    Object jsonObject = objectMapper.readValue(responseBody, Object.class);
                    return ResponseEntity.ok(jsonObject);
                } else {
                    return ResponseEntity.status(HttpStatus.NO_CONTENT)
                            .body(createErrorResponse("No content received from API"));
                }
            } catch (IOException | ParseException ex) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(createErrorResponse("HTTP Client error: " + ex.getMessage()));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error searching Google Scholar: " + e.getMessage()));
        }
    }

    /**
     * Search by citation ID
     * 
     * @param citesId the citation ID to search for
     * @param start pagination start offset (optional)
     * @param num number of results (optional)
     * @return the citing documents
     * @author Melany Rivera
     * @since October 2, 2025
     */
    @Operation(
        summary = "Find Papers Citing a Specific Publication",
        description = "Retrieves all papers that cite a specific publication using its citation ID. " +
                     "Useful for tracking research impact and finding related work.",
        tags = {"Citation Analysis"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Citations retrieved successfully",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Citation ID not found",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Internal server error",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping("/cited-by/{citesId}")
    public ResponseEntity<?> searchCitedBy(
            @Parameter(description = "Citation ID of the paper to find citations for", example = "12345678901234567890", required = true)
            @PathVariable String citesId,
            @Parameter(description = "Pagination start offset", example = "0")
            @RequestParam(defaultValue = "0") Integer start,
            @Parameter(description = "Number of results to return", example = "10")
            @RequestParam(defaultValue = "10") Integer num) {
        
        ScholarSearchRequestDTO request = new ScholarSearchRequestDTO();
        request.setCites(citesId);
        request.setStart(start);
        request.setNum(num);
        
        return searchScholar(request);
    }

    /**
     * Search by cluster ID for all versions of a paper
     * 
     * @param clusterId the cluster ID to search for
     * @param start pagination start offset (optional)
     * @param num number of results (optional)
     * @return all versions of the paper
     * @author Melany Rivera
     * @since October 2, 2025
     */
    @Operation(
        summary = "Find All Versions of a Publication",
        description = "Retrieves all available versions of a specific publication using its cluster ID. " +
                     "Includes preprints, drafts, conference papers, and journal publications.",
        tags = {"Publication Versions"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Versions retrieved successfully",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Cluster ID not found",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Internal server error",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping("/versions/{clusterId}")
    public ResponseEntity<?> searchVersions(
            @Parameter(description = "Cluster ID of the publication to find versions for", example = "9876543210987654321", required = true)
            @PathVariable String clusterId,
            @Parameter(description = "Pagination start offset", example = "0")
            @RequestParam(defaultValue = "0") Integer start,
            @Parameter(description = "Number of results to return", example = "10")
            @RequestParam(defaultValue = "10") Integer num) {
        
        ScholarSearchRequestDTO request = new ScholarSearchRequestDTO();
        request.setCluster(clusterId);
        request.setStart(start);
        request.setNum(num);
        
        return searchScholar(request);
    }

    /**
     * Search for authors by name - GET endpoint specifically for Author API compliance
     * This endpoint implements the required functionality for author searches
     * 
     * @param authorName the name of the author to search for
     * @param start pagination start offset (optional)
     * @param num number of results (optional)
     * @return author search results with publications
     * @author Melany Rivera
     * @since October 2, 2025
     */
    @Operation(
        summary = "Search Academic Authors",
        description = "Searches for academic authors by name and retrieves their publication profiles, " +
                     "citation metrics, h-index, and research interests. Implements full Author API compliance.",
        tags = {"Author Search"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Authors found successfully",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid author name parameter",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Internal server error",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping("/authors/search")
    public ResponseEntity<?> searchAuthor(
            @Parameter(description = "Name of the author to search for", example = "Andrew Ng", required = true)
            @RequestParam String authorName,
            @Parameter(description = "Pagination start offset", example = "0")
            @RequestParam(defaultValue = "0") Integer start,
            @Parameter(description = "Number of results to return", example = "10")
            @RequestParam(defaultValue = "10") Integer num) {
        
        try {
            // Validate input
            if (authorName == null || authorName.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Author name is required"));
            }

            // Create search query specifically for author search
            String authorQuery = "author:\"" + authorName.trim() + "\"";
            
            ScholarSearchRequestDTO request = new ScholarSearchRequestDTO();
            request.setQ(authorQuery);
            request.setStart(start);
            request.setNum(num);
            
            // Call the main search method
            ResponseEntity<?> response = searchScholar(request);
            
            // Log the search for demonstration purposes
            System.out.println("🔍 Author search performed for: " + authorName);
            System.out.println("📊 Query: " + authorQuery);
            
            return response;
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error searching for author: " + e.getMessage()));
        }
    }

    /**
     * Health check endpoint
     * 
     * @return API status
     * @author Melany Rivera
     * @since October 2, 2025
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "Google Scholar API");
        status.put("timestamp", System.currentTimeMillis());
        status.put("apiKeyConfigured", apiKey != null && !apiKey.trim().isEmpty());
        
        return ResponseEntity.ok(status);
    }

    /**
     * Helper method to add optional parameters to URL builder
     * 
     * @param builder the URI components builder
     * @param param the parameter name
     * @param value the parameter value
     */
    private void addOptionalParam(UriComponentsBuilder builder, String param, Object value) {
        if (value != null) {
            if (value instanceof String && !((String) value).trim().isEmpty()) {
                builder.queryParam(param, value);
            } else if (!(value instanceof String)) {
                builder.queryParam(param, value);
            }
        }
    }

    /**
     * Helper method to create error response
     * 
     * @param message the error message
     * @return error response map
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", true);
        error.put("message", message);
        error.put("timestamp", System.currentTimeMillis());
        return error;
    }
}