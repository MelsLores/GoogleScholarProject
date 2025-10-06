package com.googlescholar.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for saving Google Scholar results directly to PostgreSQL
 * 
 * @author GitHub Copilot
 * @since October 5, 2025
 */
@Service
public class ArticleDatabaseService {

    @Autowired
    private DataSource dataSource;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Create the articles table if it doesn't exist
     */
    public void createArticlesTable() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
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
            }
        }
    }

    /**
     * Parse Google Scholar JSON response and save articles to database
     * Enhanced with validation, error handling, and data integrity checks
     * 
     * @param jsonResponse The JSON response from Google Scholar API
     * @return Map containing operation status, count, errors, and validation results
     */
    public Map<String, Object> saveGoogleScholarResults(String jsonResponse) {
        Map<String, Object> result = new HashMap<>();
        int savedCount = 0;
        int duplicatesSkipped = 0;
        int validationErrors = 0;
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        try {
            // Validate input
            if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
                throw new IllegalArgumentException("JSON response cannot be null or empty");
            }

            // Ensure table exists
            createArticlesTable();

            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            
            // Validate API response structure
            if (!isValidGoogleScholarResponse(rootNode)) {
                throw new IllegalArgumentException("Invalid Google Scholar API response structure");
            }
            
            JsonNode organicResults = rootNode.get("organic_results");

            if (organicResults != null && organicResults.isArray()) {
                try (Connection connection = dataSource.getConnection()) {
                    String insertSQL = """
                        INSERT INTO articles (title, authors, publication_date, abstract, link, keywords, cited_by) 
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                        ON CONFLICT (title, authors) DO NOTHING
                        """;
                    
                    String checkDuplicateSQL = """
                        SELECT COUNT(*) FROM articles WHERE title = ? AND authors = ?
                        """;

                    try (PreparedStatement pstmt = connection.prepareStatement(insertSQL);
                         PreparedStatement checkStmt = connection.prepareStatement(checkDuplicateSQL)) {
                        
                        for (JsonNode article : organicResults) {
                            try {
                                // Validate article data
                                ArticleData articleData = extractAndValidateArticleData(article);
                                
                                if (!articleData.isValid()) {
                                    validationErrors++;
                                    warnings.add("Skipped invalid article: " + articleData.getValidationError());
                                    continue;
                                }
                                
                                // Check for duplicates
                                checkStmt.setString(1, articleData.getTitle());
                                checkStmt.setString(2, articleData.getAuthors());
                                try (var rs = checkStmt.executeQuery()) {
                                    if (rs.next() && rs.getInt(1) > 0) {
                                        duplicatesSkipped++;
                                        continue;
                                    }
                                }

                                // Insert validated data
                                pstmt.setString(1, articleData.getTitle());
                                pstmt.setString(2, articleData.getAuthors());
                                pstmt.setDate(3, articleData.getPublicationDate());
                                pstmt.setString(4, articleData.getAbstractText());
                                pstmt.setString(5, articleData.getLink());
                                pstmt.setString(6, articleData.getKeywords());
                                pstmt.setInt(7, articleData.getCitedBy());

                                int rowsAffected = pstmt.executeUpdate();
                                if (rowsAffected > 0) {
                                    savedCount++;
                                }

                            } catch (Exception e) {
                                validationErrors++;
                                errors.add("Error processing article: " + e.getMessage());
                            }
                        }
                    }
                }
            } else {
                warnings.add("No organic_results found in API response");
            }

            // Build comprehensive result
            result.put("status", savedCount > 0 ? "SUCCESS" : "PARTIAL_SUCCESS");
            result.put("message", buildResultMessage(savedCount, duplicatesSkipped, validationErrors));
            result.put("savedCount", savedCount);
            result.put("duplicatesSkipped", duplicatesSkipped);
            result.put("validationErrors", validationErrors);
            result.put("errors", errors);
            result.put("warnings", warnings);
            result.put("dataIntegrity", validateDataIntegrity());

        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("message", "Error al guardar artículos: " + e.getMessage());
            result.put("savedCount", savedCount);
        }

        return result;
    }

    /**
     * Get text from nested JSON node
     */
    private String getJsonText(JsonNode node, String... path) {
        JsonNode current = node;
        for (String key : path) {
            if (current == null || !current.has(key)) {
                return null;
            }
            current = current.get(key);
        }
        return current != null && current.isTextual() ? current.asText() : null;
    }

    /**
     * Extract authors from publication summary
     */
    private String extractAuthorsFromSummary(String summary) {
        if (summary == null) return null;
        
        // Simple regex to extract authors (names before year or journal)
        String[] parts = summary.split(" - ");
        if (parts.length > 0) {
            String authorsPart = parts[0];
            // Remove common prefixes and clean up
            authorsPart = authorsPart.replaceAll("^[A-Z]{1,3}\\s+", ""); // Remove initials
            if (authorsPart.length() > 3 && authorsPart.length() < 200) {
                return authorsPart;
            }
        }
        return null;
    }

    /**
     * Extract keywords from title (simple word extraction)
     */
    private String extractKeywords(String title) {
        if (title == null) return null;
        
        // Extract meaningful words (longer than 3 characters, not common words)
        String[] words = title.toLowerCase()
            .replaceAll("[^a-zA-Z\\s]", "")
            .split("\\s+");
            
        List<String> keywords = new ArrayList<>();
        String[] stopWords = {"the", "and", "for", "are", "but", "not", "you", "all", "can", "her", "was", "one", "our", "had", "but", "day", "get", "has", "him", "his", "how", "man", "new", "now", "old", "see", "two", "way", "who", "boy", "did", "its", "let", "put", "say", "she", "too", "use"};
        
        for (String word : words) {
            if (word.length() > 3) {
                boolean isStopWord = false;
                for (String stopWord : stopWords) {
                    if (word.equals(stopWord)) {
                        isStopWord = true;
                        break;
                    }
                }
                if (!isStopWord && keywords.size() < 5) {
                    keywords.add(word);
                }
            }
        }
        
        return String.join(", ", keywords);
    }

    /**
     * Extract publication date from summary text
     */
    private LocalDate extractPublicationDate(String summary) {
        if (summary == null) return null;
        
        // Try to find year pattern
        java.util.regex.Pattern yearPattern = java.util.regex.Pattern.compile("\\b(19|20)\\d{2}\\b");
        java.util.regex.Matcher matcher = yearPattern.matcher(summary);
        
        if (matcher.find()) {
            try {
                int year = Integer.parseInt(matcher.group());
                if (year >= 1900 && year <= LocalDate.now().getYear()) {
                    return LocalDate.of(year, 1, 1); // Default to January 1st
                }
            } catch (NumberFormatException e) {
                // Ignore
            }
        }
        
        return null;
    }
    
    /**
     * Validate Google Scholar API response structure
     * 
     * @param rootNode JSON root node
     * @return true if response structure is valid
     */
    private boolean isValidGoogleScholarResponse(JsonNode rootNode) {
        return rootNode != null && 
               (rootNode.has("organic_results") || rootNode.has("error"));
    }
    
    /**
     * Extract and validate article data from JSON node
     * 
     * @param article JSON node representing an article
     * @return ArticleData object with validation status
     */
    private ArticleData extractAndValidateArticleData(JsonNode article) {
        ArticleData data = new ArticleData();
        
        // Extract basic fields
        data.setTitle(getJsonText(article, "title"));
        data.setAuthors(getJsonText(article, "publication_info", "authors"));
        if (data.getAuthors() == null) {
            data.setAuthors(extractAuthorsFromSummary(getJsonText(article, "publication_info", "summary")));
        }
        data.setAbstractText(getJsonText(article, "snippet"));
        data.setLink(getJsonText(article, "link"));
        
        // Extract and parse cited_by count
        String citedByText = getJsonText(article, "inline_links", "cited_by", "total");
        if (citedByText == null) {
            citedByText = getJsonText(article, "cited_by", "value");
        }
        data.setCitedBy(parseCitedByCount(citedByText));
        
        // Extract keywords and publication date
        data.setKeywords(extractKeywords(data.getTitle()));
        LocalDate pubDate = extractPublicationDate(getJsonText(article, "publication_info", "summary"));
        data.setPublicationDate(pubDate != null ? java.sql.Date.valueOf(pubDate) : null);
        
        // Validate required fields
        data.validate();
        
        return data;
    }
    
    /**
     * Parse cited by count from text
     */
    private int parseCitedByCount(String citedByText) {
        if (citedByText == null) return 0;
        
        try {
            // Remove all non-numeric characters and parse
            String numericOnly = citedByText.replaceAll("[^0-9]", "");
            return numericOnly.isEmpty() ? 0 : Integer.parseInt(numericOnly);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    /**
     * Build result message based on operation statistics
     */
    private String buildResultMessage(int saved, int duplicates, int validationErrors) {
        StringBuilder message = new StringBuilder();
        message.append("Operación completada: ");
        message.append(saved).append(" artículos guardados");
        
        if (duplicates > 0) {
            message.append(", ").append(duplicates).append(" duplicados omitidos");
        }
        
        if (validationErrors > 0) {
            message.append(", ").append(validationErrors).append(" errores de validación");
        }
        
        return message.toString();
    }
    
    /**
     * Validate data integrity in the database
     */
    public Map<String, Object> validateDataIntegrity() {
        Map<String, Object> integrity = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            // Check for null or empty titles
            String nullTitlesSQL = "SELECT COUNT(*) FROM articles WHERE title IS NULL OR title = ''";
            try (PreparedStatement stmt = connection.prepareStatement(nullTitlesSQL);
                 var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    integrity.put("nullTitles", rs.getInt(1));
                }
            }
            
            // Check for articles without authors
            String noAuthorsSQL = "SELECT COUNT(*) FROM articles WHERE authors IS NULL OR authors = ''";
            try (PreparedStatement stmt = connection.prepareStatement(noAuthorsSQL);
                 var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    integrity.put("noAuthors", rs.getInt(1));
                }
            }
            
            // Check total count
            String totalSQL = "SELECT COUNT(*) FROM articles";
            try (PreparedStatement stmt = connection.prepareStatement(totalSQL);
                 var rs = stmt.executeQuery()) {
                if (rs.next()) {
                    integrity.put("totalArticles", rs.getInt(1));
                }
            }
            
            integrity.put("status", "CHECKED");
            
        } catch (Exception e) {
            integrity.put("status", "ERROR");
            integrity.put("error", e.getMessage());
        }
        
        return integrity;
    }
    
    /**
     * Inner class to hold article data with validation
     */
    private static class ArticleData {
        private String title;
        private String authors;
        private java.sql.Date publicationDate;
        private String abstractText;
        private String link;
        private String keywords;
        private int citedBy;
        private boolean valid = true;
        private String validationError;
        
        // Getters
        public String getTitle() { return title; }
        public String getAuthors() { return authors; }
        public java.sql.Date getPublicationDate() { return publicationDate; }
        public String getAbstractText() { return abstractText; }
        public String getLink() { return link; }
        public String getKeywords() { return keywords; }
        public int getCitedBy() { return citedBy; }
        public boolean isValid() { return valid; }
        public String getValidationError() { return validationError; }
        
        // Setters
        public void setTitle(String title) { this.title = title; }
        public void setAuthors(String authors) { this.authors = authors; }
        public void setPublicationDate(java.sql.Date publicationDate) { this.publicationDate = publicationDate; }
        public void setAbstractText(String abstractText) { this.abstractText = abstractText; }
        public void setLink(String link) { this.link = link; }
        public void setKeywords(String keywords) { this.keywords = keywords; }
        public void setCitedBy(int citedBy) { this.citedBy = citedBy; }
        
        /**
         * Validate article data
         */
        public void validate() {
            if (title == null || title.trim().isEmpty()) {
                valid = false;
                validationError = "Title is required";
                return;
            }
            
            if (title.length() > 255) {
                valid = false;
                validationError = "Title exceeds maximum length (255 characters)";
                return;
            }
            
            // Additional validations can be added here
            if (link != null && !isValidUrl(link)) {
                // Don't invalidate, just warn
                validationError = "Invalid URL format: " + link;
            }
        }
        
        private boolean isValidUrl(String url) {
            return url.startsWith("http://") || url.startsWith("https://");
        }
    }
}