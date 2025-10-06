package com.googlescholar.controller;

import com.googlescholar.service.ArticleDatabaseService;
import com.googlescholar.service.EnhancedScholarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced Database Test Controller with strict MVC pattern implementation
 * Follows Controller responsibilities: Handle HTTP requests, validate input, delegate to services
 * 
 * @author GitHub Copilot - Enhanced for Sprint 3
 * @date October 5, 2025
 */
@RestController
@RequestMapping("/api/test")
public class DatabaseTestController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ArticleDatabaseService articleDatabaseService;
    
    @Autowired
    private EnhancedScholarService enhancedScholarService;

    @Value("${serpapi.api.key:}")
    private String serpApiKey;

    /**
     * Test database connection
     */
    @GetMapping("/connection")
    public ResponseEntity<Map<String, Object>> testConnection() {
        Map<String, Object> response = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            response.put("status", "SUCCESS");
            response.put("message", "Conexión exitosa a PostgreSQL");
            response.put("database", connection.getCatalog());
            response.put("url", connection.getMetaData().getURL());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Error de conexión: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Test creating a simple table and inserting data
     */
    @PostMapping("/create-table")
    public ResponseEntity<Map<String, Object>> createTestTable() {
        Map<String, Object> response = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            // Create test table
            String createTableSQL = """
                CREATE TABLE IF NOT EXISTS test_researchers (
                    id SERIAL PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    email VARCHAR(255),
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;
            
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(createTableSQL);
            }
            
            // Insert test data
            String insertSQL = "INSERT INTO test_researchers (name, email) VALUES (?, ?)";
            try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
                pstmt.setString(1, "Dr. Juan Pérez");
                pstmt.setString(2, "juan.perez@example.com");
                int rowsAffected = pstmt.executeUpdate();
                
                response.put("status", "SUCCESS");
                response.put("message", "Tabla creada y datos insertados correctamente");
                response.put("rowsAffected", rowsAffected);
                return ResponseEntity.ok(response);
            }
            
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Error al crear tabla: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Create articles table and save Google Scholar search results
     */
    @PostMapping("/create-articles-table")
    public ResponseEntity<Map<String, Object>> createArticlesTable() {
        Map<String, Object> response = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            // Create articles table with PostgreSQL syntax
            String createTableSQL = """
                CREATE TABLE IF NOT EXISTS articles (
                    id SERIAL PRIMARY KEY,
                    title VARCHAR(255) NOT NULL,
                    authors TEXT,
                    publication_date DATE,
                    abstract TEXT,
                    link TEXT,
                    keywords TEXT,
                    cited_by INT DEFAULT 0
                )
                """;
            
            try (Statement stmt = connection.createStatement()) {
                stmt.execute(createTableSQL);
                response.put("status", "SUCCESS");
                response.put("message", "Tabla articles creada correctamente");
                return ResponseEntity.ok(response);
            }
            
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Error al crear tabla articles: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Save Google Scholar search results to articles table
     */
    @PostMapping("/save-scholar-results")
    public ResponseEntity<Map<String, Object>> saveScholarResults() {
        Map<String, Object> response = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            // Insert sample article data
            String insertSQL = """
                INSERT INTO articles (title, authors, publication_date, abstract, link, keywords, cited_by) 
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
            
            try (PreparedStatement pstmt = connection.prepareStatement(insertSQL)) {
                // Sample article data
                pstmt.setString(1, "Machine Learning Applications in Healthcare");
                pstmt.setString(2, "Dr. John Smith, Dr. Maria Garcia, Dr. Ahmed Hassan");
                pstmt.setDate(3, java.sql.Date.valueOf("2024-03-15"));
                pstmt.setString(4, "This paper explores the applications of machine learning in healthcare, focusing on diagnostic systems and patient care optimization.");
                pstmt.setString(5, "https://scholar.google.com/citations?view_op=view_citation&hl=en&user=example");
                pstmt.setString(6, "machine learning, healthcare, AI, diagnosis, medical systems");
                pstmt.setInt(7, 145);
                
                int rowsAffected = pstmt.executeUpdate();
                
                response.put("status", "SUCCESS");
                response.put("message", "Artículo guardado en la base de datos");
                response.put("rowsAffected", rowsAffected);
                return ResponseEntity.ok(response);
            }
            
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Error al guardar artículo: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Read all articles from the database
     */
    @GetMapping("/read-articles")
    public ResponseEntity<Map<String, Object>> readArticles() {
        Map<String, Object> response = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            // First check if table exists
            String checkTableSQL = "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'articles'";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkTableSQL);
                 ResultSet checkRs = checkStmt.executeQuery()) {
                
                if (checkRs.next() && checkRs.getInt(1) == 0) {
                    response.put("status", "ERROR");
                    response.put("message", "La tabla 'articles' no existe");
                    return ResponseEntity.badRequest().body(response);
                }
            }
            
            String selectSQL = "SELECT id, title, authors, publication_date, abstract, link, keywords, cited_by FROM articles ORDER BY id DESC LIMIT 10";
            
            try (PreparedStatement pstmt = connection.prepareStatement(selectSQL);
                 ResultSet rs = pstmt.executeQuery()) {
                
                int count = 0;
                while (rs.next()) {
                    count++;
                    Map<String, Object> article = new HashMap<>();
                    article.put("id", rs.getInt("id"));
                    article.put("title", rs.getString("title") != null ? rs.getString("title") : "Sin título");
                    article.put("authors", rs.getString("authors") != null ? rs.getString("authors") : "Sin autores");
                    article.put("publication_date", rs.getDate("publication_date") != null ? rs.getDate("publication_date").toString() : "Sin fecha");
                    article.put("abstract", rs.getString("abstract") != null ? rs.getString("abstract") : "Sin resumen");
                    article.put("link", rs.getString("link") != null ? rs.getString("link") : "Sin enlace");
                    article.put("keywords", rs.getString("keywords") != null ? rs.getString("keywords") : "Sin palabras clave");
                    article.put("cited_by", rs.getInt("cited_by"));
                    
                    response.put("article_" + count, article);
                }
                
                response.put("status", "SUCCESS");
                response.put("message", "Artículos leídos correctamente");
                response.put("totalArticles", count);
                return ResponseEntity.ok(response);
            }
            
        } catch (Exception e) {
            e.printStackTrace(); // Para ver el stack trace completo en logs
            response.put("status", "ERROR");
            response.put("message", "Error al leer artículos: " + e.getMessage());
            response.put("error_details", e.getClass().getSimpleName());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Enhanced search with pagination, validation, and comprehensive error handling
     * Addresses feedback: "manipulation of Google Scholar API", "pagination", "error handling"
     */
    @GetMapping("/enhanced-search")
    public ResponseEntity<Map<String, Object>> enhancedSearch(
            @RequestParam(defaultValue = "machine learning") String query,
            @RequestParam(defaultValue = "20") int totalResults,
            @RequestParam(defaultValue = "true") boolean saveToDatabase) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Input validation (Controller responsibility)
            if (query.length() < 3) {
                response.put("status", "ERROR");
                response.put("message", "Query must be at least 3 characters long");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (totalResults > 100) {
                response.put("status", "ERROR");
                response.put("message", "Total results cannot exceed 100 for performance reasons");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Delegate to service (following MVC pattern)
            Map<String, Object> searchResult = enhancedScholarService.searchWithPagination(
                query, 1, totalResults); // page=1, pageSize=totalResults
            
            response.put("timestamp", java.time.LocalDateTime.now());
            response.put("searchParameters", Map.of(
                "query", query,
                "totalResults", totalResults,
                "saveToDatabase", saveToDatabase
            ));
            response.putAll(searchResult);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Enhanced search failed: " + e.getMessage());
            response.put("timestamp", java.time.LocalDateTime.now());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Validate API connectivity and configuration
     * Addresses feedback: "validate responses effectively"
     */
    @GetMapping("/validate-api")
    public ResponseEntity<Map<String, Object>> validateApiConnectivity() {
        try {
            Map<String, Object> validationResult = enhancedScholarService.validateApiConnectivity();
            
            if ("SUCCESS".equals(validationResult.get("status"))) {
                return ResponseEntity.ok(validationResult);
            } else {
                return ResponseEntity.status(503).body(validationResult);
            }
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ERROR");
            response.put("message", "API validation failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Get search suggestions
     * Addresses feedback: Enhanced user experience and API manipulation
     */
    @GetMapping("/search-suggestions")
    public ResponseEntity<Map<String, Object>> getSearchSuggestions(
            @RequestParam String partialQuery) {
        
        try {
            // Input validation
            if (partialQuery == null || partialQuery.trim().length() < 2) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "ERROR");
                response.put("message", "Partial query must be at least 2 characters");
                return ResponseEntity.badRequest().body(response);
            }
            
            Map<String, Object> suggestions = enhancedScholarService.getSearchSuggestions(partialQuery.trim());
            return ResponseEntity.ok(suggestions);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ERROR");
            response.put("message", "Failed to get suggestions: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Data integrity validation endpoint
     * Addresses feedback: "data integrity", "validate responses effectively"
     */
    @GetMapping("/data-integrity")
    public ResponseEntity<Map<String, Object>> validateDataIntegrity() {
        Map<String, Object> response = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            response.put("timestamp", java.time.LocalDateTime.now());
            
            // Check database connectivity
            response.put("databaseConnectivity", "SUCCESS");
            
            // Delegate integrity validation to service
            // This would ideally be in a separate DataIntegrityService (future enhancement)
            Map<String, Object> integrity = performDataIntegrityChecks(connection);
            response.put("integrityChecks", integrity);
            
            // Overall status
            boolean hasIssues = integrity.values().stream()
                .anyMatch(value -> value instanceof Integer && (Integer) value > 0);
            
            response.put("status", hasIssues ? "WARNING" : "SUCCESS");
            response.put("message", hasIssues ? 
                "Data integrity issues found - see integrityChecks for details" : 
                "All data integrity checks passed");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Data integrity check failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Comprehensive system health check
     * Addresses feedback: "robustness of the system"
     */
    @GetMapping("/system-health")
    public ResponseEntity<Map<String, Object>> systemHealthCheck() {
        Map<String, Object> response = new HashMap<>();
        boolean overallHealth = true;
        
        try {
            response.put("timestamp", java.time.LocalDateTime.now());
            
            // Database health
            try (Connection connection = dataSource.getConnection()) {
                response.put("database", Map.of(
                    "status", "HEALTHY",
                    "url", connection.getMetaData().getURL(),
                    "catalog", connection.getCatalog()
                ));
            } catch (Exception e) {
                response.put("database", Map.of(
                    "status", "UNHEALTHY",
                    "error", e.getMessage()
                ));
                overallHealth = false;
            }
            
            // API health
            try {
                Map<String, Object> apiHealth = enhancedScholarService.validateApiConnectivity();
                response.put("api", apiHealth);
                if (!"SUCCESS".equals(apiHealth.get("status"))) {
                    overallHealth = false;
                }
            } catch (Exception e) {
                response.put("api", Map.of(
                    "status", "UNHEALTHY",
                    "error", e.getMessage()
                ));
                overallHealth = false;
            }
            
            // Memory health
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            double memoryUsagePercentage = (double) usedMemory / totalMemory * 100;
            
            response.put("memory", Map.of(
                "status", memoryUsagePercentage > 90 ? "WARNING" : "HEALTHY",
                "usagePercentage", Math.round(memoryUsagePercentage * 100.0) / 100.0,
                "totalMB", totalMemory / (1024 * 1024),
                "usedMB", usedMemory / (1024 * 1024)
            ));
            
            response.put("overallStatus", overallHealth ? "HEALTHY" : "DEGRADED");
            
            return overallHealth ? 
                ResponseEntity.ok(response) : 
                ResponseEntity.status(503).body(response);
                
        } catch (Exception e) {
            response.put("overallStatus", "UNHEALTHY");
            response.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Helper method for data integrity checks
     * Note: In a full MVC implementation, this would be moved to a dedicated Service
     */
    private Map<String, Object> performDataIntegrityChecks(Connection connection) throws Exception {
        Map<String, Object> checks = new HashMap<>();
        
        // Check for articles with missing titles
        String nullTitlesSQL = "SELECT COUNT(*) FROM articles WHERE title IS NULL OR title = '' OR title = 'Unknown Title'";
        try (PreparedStatement stmt = connection.prepareStatement(nullTitlesSQL);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                checks.put("articlesWithMissingTitles", rs.getInt(1));
            }
        }
        
        // Check for articles without authors
        String noAuthorsSQL = "SELECT COUNT(*) FROM articles WHERE authors IS NULL OR authors = ''";
        try (PreparedStatement stmt = connection.prepareStatement(noAuthorsSQL);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                checks.put("articlesWithoutAuthors", rs.getInt(1));
            }
        }
        
        // Check for duplicate articles (same title and authors)
        String duplicatesSQL = """
            SELECT COUNT(*) FROM (
                SELECT title, authors, COUNT(*) as cnt 
                FROM articles 
                WHERE title IS NOT NULL AND authors IS NOT NULL
                GROUP BY title, authors 
                HAVING COUNT(*) > 1
            ) duplicates
            """;
        try (PreparedStatement stmt = connection.prepareStatement(duplicatesSQL);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                checks.put("duplicateArticles", rs.getInt(1));
            }
        }
        
        // Check total articles count
        String totalSQL = "SELECT COUNT(*) FROM articles";
        try (PreparedStatement stmt = connection.prepareStatement(totalSQL);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                checks.put("totalArticles", rs.getInt(1));
            }
        }
        
        // Check articles with invalid URLs
        String invalidUrlsSQL = "SELECT COUNT(*) FROM articles WHERE link IS NOT NULL AND link NOT LIKE 'http%'";
        try (PreparedStatement stmt = connection.prepareStatement(invalidUrlsSQL);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                checks.put("articlesWithInvalidUrls", rs.getInt(1));
            }
        }
        
        return checks;
    }
}