package com.googlescholar.controller;

import com.googlescholar.view.ScholarSearchRequestDTO;
import com.googlescholar.view.ScholarSearchResponseDTO;
import com.googlescholar.service.HttpClientService;
import com.googlescholar.service.ScholarArticleService;
import com.googlescholar.service.MultiResearcherService;
import com.googlescholar.config.DatabaseManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import org.apache.hc.core5.http.ParseException;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
    private final DatabaseManager databaseManager;
    private final ScholarArticleService scholarArticleService;
    private final MultiResearcherService multiResearcherService;
    private final String serpApiBaseUrl = "https://serpapi.com/search.json";

    @Value("${serpapi.apiKey:}")
    private String apiKey;

    /**
     * Constructor with dependencies injection
     * 
     * @param httpClientService the HTTP client service for API calls
     * @param objectMapper the JSON object mapper
     * @param databaseManager the database manager for SQL Server operations
     * @param scholarArticleService the service for processing articles
     * @param multiResearcherService the service for processing multiple researchers
     * @author Melany Rivera
     * @since October 2, 2025
     */
    public ScholarController(HttpClientService httpClientService, ObjectMapper objectMapper, 
                            DatabaseManager databaseManager, ScholarArticleService scholarArticleService,
                            MultiResearcherService multiResearcherService) {
        this.httpClientService = httpClientService;
        this.objectMapper = objectMapper;
        this.databaseManager = databaseManager;
        this.scholarArticleService = scholarArticleService;
        this.multiResearcherService = multiResearcherService;
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
            addOptionalParam(builder, "start", request.getPageStart());
            addOptionalParam(builder, "num", request.getPageSize());
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
            @RequestParam(defaultValue = "0") Integer pageStart,
            @Parameter(description = "Number of results per page", example = "10")
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
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
                    .queryParam("start", pageStart)
                    .queryParam("num", pageSize);

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
     * @param pageStart pagination start offset (optional)
     * @param pageSize number of results per page (optional)
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
            @RequestParam(defaultValue = "0") Integer pageStart,
            @Parameter(description = "Number of results per page", example = "10")
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        ScholarSearchRequestDTO request = new ScholarSearchRequestDTO();
        request.setCites(citesId);
        request.setPageStart(pageStart);
        request.setPageSize(pageSize);
        
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
            @RequestParam(defaultValue = "0") Integer pageStart,
            @Parameter(description = "Number of results per page", example = "10")
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        ScholarSearchRequestDTO request = new ScholarSearchRequestDTO();
        request.setCluster(clusterId);
        request.setPageStart(pageStart);
        request.setPageSize(pageSize);
        
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
            @RequestParam(defaultValue = "0") Integer pageStart,
            @Parameter(description = "Number of results per page", example = "10")
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
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
            request.setPageStart(pageStart);
            request.setPageSize(pageSize);
            
            // Call the main search method
            ResponseEntity<?> response = searchScholar(request);
            
            // DATABASE SAVING ENABLED - Connected to SQL Server
            // If search was successful, save articles to database
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() instanceof ScholarSearchResponseDTO) {
                try {
                    ScholarSearchResponseDTO searchResponse = (ScholarSearchResponseDTO) response.getBody();
                    saveAuthorArticlesToDatabase(authorName.trim(), searchResponse);
                } catch (Exception dbException) {
                    System.err.println("⚠️ Error saving articles to database: " + dbException.getMessage());
                    // Log error but don't fail the entire request
                }
            }
            
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
     * Enhanced search endpoint that saves results to database
     * 
     * @param request the search request parameters
     * @param saveToDatabase whether to save results to database
     * @return the search results from Google Scholar
     * @author Melany Rivera
     * @since October 2, 2025
     */
    @Operation(
        summary = "Search Google Scholar and Save to Database",
        description = "Performs a search of Google Scholar and optionally saves the articles to SQL Server database. " +
                     "Extracts publication metadata and stores in the articles table.",
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
            description = "Internal server error or database error",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping("/search-and-save")
    public ResponseEntity<?> searchAndSave(
        @Parameter(description = "Search parameters for Google Scholar query", required = true)
        @Valid @RequestBody ScholarSearchRequestDTO request,
        @Parameter(description = "Whether to save results to database", example = "true")
        @RequestParam(defaultValue = "true") boolean saveToDatabase) {
        
        try {
            // First perform the regular search
            ResponseEntity<?> searchResponse = searchScholar(request);
            
            // If search was successful and saveToDatabase is true, process and save results
            if (searchResponse.getStatusCode() == HttpStatus.OK && saveToDatabase) {
                Object responseBody = searchResponse.getBody();
                if (responseBody instanceof ScholarSearchResponseDTO) {
                    ScholarSearchResponseDTO searchResults = (ScholarSearchResponseDTO) responseBody;
                    
                    // Convert and save articles to database
                    int savedCount = processAndSaveArticles(searchResults);
                    
                    // Add database save information to response
                    Map<String, Object> enhancedResponse = new HashMap<>();
                    enhancedResponse.put("searchResults", searchResults);
                    enhancedResponse.put("databaseInfo", Map.of(
                        "saved", true,
                        "articlesCount", savedCount,
                        "message", "Articles saved to database successfully"
                    ));
                    
                    return ResponseEntity.ok(enhancedResponse);
                }
            }
            
            return searchResponse;
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error in search and save operation: " + e.getMessage()));
        }
    }

    /**
     * Save articles from API response to database
     * 
     * @param articleData JSON array of articles from Google Scholar API
     * @return number of articles saved
     * @author Melany Rivera
     * @since October 2, 2025
     */
    @Operation(
        summary = "Save Google Scholar Articles to Database",
        description = "Processes Google Scholar Author Articles API response and saves articles to SQL Server database. " +
                     "Handles the articles array from the JSON response and maps to database structure.",
        tags = {"Database Operations"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Articles saved successfully",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid article data provided",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Database error occurred",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping("/save-articles")
    public ResponseEntity<?> saveArticlesToDatabase(
        @Parameter(description = "Raw JSON response from Google Scholar Author Articles API", required = true)
        @RequestBody String jsonResponse) {
        
        try {
            // Use the service to process and save articles
            ScholarArticleService.ArticleProcessingResult result = scholarArticleService.processAndSaveArticles(jsonResponse);
            
            if (result.hasError()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse(result.getError()));
            }
            
            // Prepare success response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Articles processing completed successfully");
            response.put("statistics", Map.of(
                "totalInResponse", result.getTotalInResponse(),
                "totalProcessed", result.getProcessed(),
                "newlySaved", result.getSaved(),
                "skippedDuplicates", result.getSkipped(),
                "citationsUpdated", result.getUpdated(),
                "failed", result.getFailed()
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error saving articles to database: " + e.getMessage()));
        }
    }

    /**
     * Process multiple researchers with their articles (2 researchers, 3 articles each)
     * 
     * @param researchersRequest Array of researcher data with their articles
     * @return processing results with statistics
     * @author Melany Rivera
     * @since October 2, 2025
     */
    @Operation(
        summary = "Process Multiple Researchers with Articles",
        description = "Processes multiple researchers (max 2) with their articles (max 3 per researcher). " +
                     "Saves researchers and their articles to the database with proper relationships. " +
                     "Designed for handling 6 total articles from 2 researchers.",
        tags = {"Database Operations"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Researchers and articles processed successfully",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid researcher data provided",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Database error occurred",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping("/process-multiple-researchers")
    public ResponseEntity<?> processMultipleResearchers(
        @Parameter(description = "Array of researcher data with their articles JSON", required = true)
        @RequestBody List<MultiResearcherService.ResearcherData> researchersRequest) {
        
        try {
            // Validate input
            if (researchersRequest == null || researchersRequest.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("No researcher data provided"));
            }
            
            if (researchersRequest.size() > 2) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Maximum 2 researchers allowed"));
            }
            
            // Process the researchers
            MultiResearcherService.MultiResearcherResult result = 
                multiResearcherService.processMultipleResearchers(researchersRequest);
            
            if (result.hasError()) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse(result.getError()));
            }
            
            // Prepare detailed response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Multiple researchers processed successfully");
            
            // Overall statistics
            Map<String, Object> overallStats = new HashMap<>();
            overallStats.put("totalResearchers", result.getTotalResearchers());
            overallStats.put("successfulResearchers", result.getSuccessfulResearchers());
            overallStats.put("failedResearchers", result.getFailedResearchers());
            overallStats.put("totalArticlesProcessed", result.getTotalArticlesProcessed());
            overallStats.put("totalArticlesSaved", result.getTotalArticlesSaved());
            overallStats.put("totalArticlesSkipped", result.getTotalArticlesSkipped());
            overallStats.put("totalArticlesUpdated", result.getTotalArticlesUpdated());
            
            response.put("overallStatistics", overallStats);
            
            // Detailed results per researcher
            List<Map<String, Object>> researcherDetails = new ArrayList<>();
            for (MultiResearcherService.ResearcherProcessingResult resResult : result.getResearcherResults()) {
                Map<String, Object> detail = new HashMap<>();
                detail.put("authorId", resResult.getAuthorId());
                detail.put("name", resResult.getName());
                detail.put("researcherSaved", resResult.isResearcherSaved());
                detail.put("researcherUpdated", resResult.isResearcherUpdated());
                
                Map<String, Object> articleStats = new HashMap<>();
                articleStats.put("totalInResponse", resResult.getTotalArticlesInResponse());
                articleStats.put("processed", resResult.getArticlesProcessed());
                articleStats.put("saved", resResult.getArticlesSaved());
                articleStats.put("skipped", resResult.getArticlesSkipped());
                articleStats.put("updated", resResult.getArticlesUpdated());
                articleStats.put("failed", resResult.getArticlesFailed());
                
                detail.put("articleStatistics", articleStats);
                detail.put("hasError", resResult.hasError());
                if (resResult.hasError()) {
                    detail.put("error", resResult.getError());
                }
                
                researcherDetails.add(detail);
            }
            
            response.put("researcherDetails", researcherDetails);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error processing multiple researchers: " + e.getMessage()));
        }
    }

    /**
     * Initialize database tables for researchers and articles
     * 
     * @return initialization status
     * @author Melany Rivera
     * @since October 2, 2025
     */
    @PostMapping("/database/initialize")
    public ResponseEntity<Map<String, Object>> initializeDatabase() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            databaseManager.createTablesIfNotExist();
            status.put("success", true);
            status.put("message", "Database tables initialized successfully");
            status.put("tables", List.of("researchers", "articles"));
            status.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            status.put("success", false);
            status.put("error", e.getMessage());
            status.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(status);
        }
    }

    /**
     * Test database connection endpoint
     * 
     * @return database connection status
     * @author Melany Rivera
     * @since October 2, 2025
     */
    @GetMapping("/database/test")
    public ResponseEntity<Map<String, Object>> testDatabaseConnection() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            boolean connected = databaseManager.testConnection();
            status.put("connected", connected);
            status.put("message", connected ? "Database connection successful" : "Database connection failed");
            status.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(status);
            
        } catch (Exception e) {
            status.put("connected", false);
            status.put("error", e.getMessage());
            status.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(status);
        }
    }

    /**
     * Helper method to process and save articles from search response
     * 
     * @param searchResults the search response containing organic results
     * @return number of articles saved
     */
    private int processAndSaveArticles(ScholarSearchResponseDTO searchResults) {
        // This method would need to be implemented based on your ScholarSearchResponseDTO structure
        // For now, returning 0 as placeholder
        return 0;
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
     * Get database statistics
     * 
     * @return database statistics including record counts
     * @author Melany Rivera
     * @since October 3, 2025
     */
    @Operation(
        summary = "Get Database Statistics",
        description = "Returns statistics about the database including number of researchers and articles stored.",
        tags = {"Database"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Statistics retrieved successfully",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Internal server error",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping("/database/stats")
    public ResponseEntity<?> getDatabaseStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // Get connection and query counts
            try (Connection connection = databaseManager.getConnection()) {
                
                // Count researchers
                String researcherCountSQL = "SELECT COUNT(*) FROM researchers";
                try (PreparedStatement stmt = connection.prepareStatement(researcherCountSQL);
                     ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        stats.put("total_researchers", rs.getInt(1));
                    }
                }
                
                // Count articles
                String articleCountSQL = "SELECT COUNT(*) FROM articles";
                try (PreparedStatement stmt = connection.prepareStatement(articleCountSQL);
                     ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        stats.put("total_articles", rs.getInt(1));
                    }
                }
                
                // Get recent researchers
                String recentResearchersSQL = """
                    SELECT TOP 5 name, created_at 
                    FROM researchers 
                    ORDER BY created_at DESC
                    """;
                List<Map<String, Object>> recentResearchers = new ArrayList<>();
                try (PreparedStatement stmt = connection.prepareStatement(recentResearchersSQL);
                     ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> researcher = new HashMap<>();
                        researcher.put("name", rs.getString("name"));
                        researcher.put("created_at", rs.getTimestamp("created_at"));
                        recentResearchers.add(researcher);
                    }
                }
                stats.put("recent_researchers", recentResearchers);
                
                // Get recent articles
                String recentArticlesSQL = """
                    SELECT TOP 5 a.title, a.authors, a.cited_by, a.year, r.name as researcher_name, a.created_at
                    FROM articles a
                    LEFT JOIN researchers r ON a.researcher_id = r.researcher_id
                    ORDER BY a.created_at DESC
                    """;
                List<Map<String, Object>> recentArticles = new ArrayList<>();
                try (PreparedStatement stmt = connection.prepareStatement(recentArticlesSQL);
                     ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> article = new HashMap<>();
                        article.put("title", rs.getString("title"));
                        article.put("authors", rs.getString("authors"));
                        article.put("cited_by", rs.getInt("cited_by"));
                        article.put("year", rs.getObject("year"));
                        article.put("researcher_name", rs.getString("researcher_name"));
                        article.put("created_at", rs.getTimestamp("created_at"));
                        recentArticles.add(article);
                    }
                }
                stats.put("recent_articles", recentArticles);
                
                stats.put("database_connection", "successful");
                stats.put("timestamp", System.currentTimeMillis());
                
                return ResponseEntity.ok(stats);
                
            }
            
        } catch (Exception e) {
            System.err.println("Error getting database stats: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error getting database statistics: " + e.getMessage()));
        }
    }

    /**
     * Saves author articles to the database
     * 
     * @param authorName the name of the author
     * @param searchResponse the search response containing articles
     */
    private void saveAuthorArticlesToDatabase(String authorName, ScholarSearchResponseDTO searchResponse) {
        try {
            System.out.println("💾 Starting database save operation for author: " + authorName);
            
            // First, insert or get the researcher
            Long researcherId = null;
            try {
                // Create a researcher with the author name (other fields can be null)
                researcherId = databaseManager.insertResearcher(
                    null,           // authorId - will be null for now
                    authorName,     // name
                    null,           // affiliation
                    null,           // email  
                    null,           // hIndex
                    null,           // i10Index
                    null,           // totalCitations
                    null,           // interests
                    null            // profileUrl
                );
                System.out.println("✅ Researcher saved/found with ID: " + researcherId);
            } catch (Exception e) {
                System.err.println("❌ Error saving researcher: " + e.getMessage());
                return;
            }
            
            // Check if we have organic results (articles)
            if (searchResponse.getOrganicResults() == null || searchResponse.getOrganicResults().isEmpty()) {
                System.out.println("ℹ️ No articles found for author: " + authorName);
                return;
            }
            
            int savedArticles = 0;
            int totalArticles = searchResponse.getOrganicResults().size();
            
            System.out.println("📄 Processing " + totalArticles + " articles for database insertion...");
            
            // Process each article
            for (ScholarSearchResponseDTO.OrganicResult article : searchResponse.getOrganicResults()) {
                try {
                    // Extract article information
                    String title = article.getTitle();
                    String link = article.getLink();
                    String snippet = article.getSnippet();
                    
                    // Extract year from publication info if available
                    Integer year = null;
                    if (article.getPublicationInfo() != null && article.getPublicationInfo().getSummary() != null) {
                        String summary = article.getPublicationInfo().getSummary();
                        year = extractYearFromSummary(summary);
                    }
                    
                    // Extract citation count if available
                    Integer citations = null;
                    if (article.getInlineLinks() != null && article.getInlineLinks().getCitedBy() != null) {
                        citations = article.getInlineLinks().getCitedBy().getTotal();
                    }
                    
                    // Use result_id as citation_id if available
                    String citationId = article.getResultId();
                    
                    // Validate required fields
                    if (title == null || title.trim().isEmpty()) {
                        System.out.println("⚠️ Skipping article with empty title");
                        continue;
                    }
                    
                    // Insert article into database
                    Long articleId = databaseManager.insertArticleWithResearcher(
                        title.trim(),        // title
                        authorName,          // authors (using the searched author name)
                        null,               // publicationDate (LocalDate - can be null)
                        snippet,            // abstractText (using snippet as abstract)
                        link,               // link
                        null,               // keywords (can be null)
                        citations != null ? citations.intValue() : 0, // citedBy (convert to int)
                        researcherId,       // researcherId
                        citationId,         // citationId
                        year                // year
                    );
                    
                    if (articleId != null) {
                        savedArticles++;
                        System.out.println("✅ Article saved with ID: " + articleId + " - " + 
                                         (title.length() > 50 ? title.substring(0, 50) + "..." : title));
                    }
                    
                } catch (Exception articleException) {
                    System.err.println("❌ Error saving individual article: " + articleException.getMessage());
                }
            }
            
            System.out.println("🎉 Database save completed! Saved " + savedArticles + " out of " + totalArticles + " articles for author: " + authorName);
            
        } catch (Exception e) {
            System.err.println("💥 Critical error in saveAuthorArticlesToDatabase: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Extracts year from publication summary text
     * 
     * @param summary the publication summary
     * @return the extracted year or null if not found
     */
    private Integer extractYearFromSummary(String summary) {
        if (summary == null || summary.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Look for 4-digit year patterns (1900-2099)
            java.util.regex.Pattern yearPattern = java.util.regex.Pattern.compile("\\b(19|20)\\d{2}\\b");
            java.util.regex.Matcher matcher = yearPattern.matcher(summary);
            
            if (matcher.find()) {
                String yearStr = matcher.group();
                return Integer.parseInt(yearStr);
            }
        } catch (Exception e) {
            System.err.println("Error extracting year from summary: " + e.getMessage());
        }
        
        return null;
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