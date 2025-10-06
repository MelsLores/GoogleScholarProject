package com.googlescholar.service;

import com.googlescholar.config.DatabaseManager;
import com.googlescholar.model.Article;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Logger;

/**
 * Service for handling Google Scholar API data and database operations
 * Specifically designed to process the Google Scholar Author Articles API response
 * 
 * @author Melany Rivera
 * @since October 2, 2025
 */
@Service
public class ScholarArticleService {
    
    private static final Logger LOGGER = Logger.getLogger(ScholarArticleService.class.getName());
    
    private final DatabaseManager databaseManager;
    private final ObjectMapper objectMapper;
    
    public ScholarArticleService(DatabaseManager databaseManager, ObjectMapper objectMapper) {
        this.databaseManager = databaseManager;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Processes the JSON response from Google Scholar Author Articles API
     * and saves the articles to the database
     * 
     * @param jsonResponse Raw JSON response from SerpApi Google Scholar Author Articles
     * @return Processing statistics
     */
    public ArticleProcessingResult processAndSaveArticles(String jsonResponse) {
        ArticleProcessingResult result = new ArticleProcessingResult();
        
        try {
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode articlesNode = rootNode.get("articles");
            
            if (articlesNode == null || !articlesNode.isArray()) {
                result.setError("Invalid JSON: 'articles' array not found");
                return result;
            }
            
            result.setTotalInResponse(articlesNode.size());
            
            for (JsonNode articleNode : articlesNode) {
                try {
                    Article article = parseArticleFromJson(articleNode);
                    if (article != null && article.getTitle() != null && !article.getTitle().trim().isEmpty()) {
                        
                        // Check if article already exists
                        if (!databaseManager.articleExists(article.getTitle())) {
                            long articleId = databaseManager.insertArticle(
                                article.getTitle(),
                                article.getAuthors(),
                                article.getPublicationDate(),
                                article.getAbstractText(),
                                article.getLink(),
                                article.getKeywords(),
                                article.getCitedBy() != null ? article.getCitedBy() : 0
                            );
                            
                            if (articleId > 0) {
                                result.incrementSaved();
                                LOGGER.info("Saved article: " + article.getTitle());
                            } else {
                                result.incrementFailed();
                            }
                        } else {
                            // Update citation count if article exists
                            if (article.getCitedBy() != null) {
                                boolean updated = databaseManager.updateCitationCount(article.getTitle(), article.getCitedBy());
                                if (updated) {
                                    result.incrementUpdated();
                                    LOGGER.info("Updated citation count for: " + article.getTitle());
                                }
                            }
                            result.incrementSkipped();
                        }
                        result.incrementProcessed();
                    }
                } catch (Exception e) {
                    result.incrementFailed();
                    LOGGER.warning("Error processing individual article: " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            result.setError("Error processing JSON response: " + e.getMessage());
            LOGGER.severe("Error processing articles: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Parses a single article from the JSON node structure
     * Based on the Google Scholar Author Articles API response format
     */
    private Article parseArticleFromJson(JsonNode articleNode) {
        try {
            String title = getJsonValue(articleNode, "title");
            String authors = getJsonValue(articleNode, "authors");
            String link = getJsonValue(articleNode, "link");
            String publication = getJsonValue(articleNode, "publication");
            
            // Extract year from publication string
            LocalDate publicationDate = extractDateFromPublication(publication);
            
            // Get cited_by count
            Integer citedBy = 0;
            JsonNode citedByNode = articleNode.get("cited_by");
            if (citedByNode != null && citedByNode.has("value")) {
                citedBy = citedByNode.get("value").asInt(0);
            }
            
            // Extract year as keywords (simple approach)
            String keywords = extractKeywordsFromPublication(publication);
            
            Article article = new Article();
            article.setTitle(title);
            article.setAuthors(authors);
            article.setPublicationDate(publicationDate);
            article.setAbstractText(null); // Not available in Author Articles API
            article.setLink(link);
            article.setKeywords(keywords);
            article.setCitedBy(citedBy);
            
            return article;
            
        } catch (Exception e) {
            LOGGER.warning("Error parsing article from JSON: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Safely extracts string value from JSON node
     */
    private String getJsonValue(JsonNode node, String fieldName) {
        JsonNode fieldNode = node.get(fieldName);
        return (fieldNode != null && !fieldNode.isNull()) ? fieldNode.asText() : null;
    }
    
    /**
     * Extracts publication date from publication string
     * Example: "Nature 564 (7736), 386-390, 2018" -> LocalDate(2018, 1, 1)
     */
    private LocalDate extractDateFromPublication(String publication) {
        if (publication == null || publication.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Look for 4-digit year
            Pattern yearPattern = Pattern.compile("\\b(19|20)\\d{2}\\b");
            Matcher matcher = yearPattern.matcher(publication);
            
            if (matcher.find()) {
                int year = Integer.parseInt(matcher.group());
                return LocalDate.of(year, 1, 1);
            }
        } catch (Exception e) {
            LOGGER.warning("Error extracting date from publication: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Extracts keywords from publication string
     * Simple approach: uses journal name and year
     */
    private String extractKeywordsFromPublication(String publication) {
        if (publication == null || publication.trim().isEmpty()) {
            return null;
        }
        
        try {
            // Extract journal name (everything before the first comma or number)
            Pattern journalPattern = Pattern.compile("^([^,\\d]+)");
            Matcher matcher = journalPattern.matcher(publication.trim());
            
            if (matcher.find()) {
                String journalName = matcher.group(1).trim();
                
                // Extract year
                Pattern yearPattern = Pattern.compile("\\b(19|20)\\d{2}\\b");
                Matcher yearMatcher = yearPattern.matcher(publication);
                
                if (yearMatcher.find()) {
                    String year = yearMatcher.group();
                    return journalName + ", " + year;
                }
                
                return journalName;
            }
        } catch (Exception e) {
            LOGGER.warning("Error extracting keywords from publication: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Result class for article processing operations
     */
    public static class ArticleProcessingResult {
        private int totalInResponse = 0;
        private int processed = 0;
        private int saved = 0;
        private int skipped = 0;
        private int updated = 0;
        private int failed = 0;
        private String error = null;
        
        // Getters
        public int getTotalInResponse() { return totalInResponse; }
        public int getProcessed() { return processed; }
        public int getSaved() { return saved; }
        public int getSkipped() { return skipped; }
        public int getUpdated() { return updated; }
        public int getFailed() { return failed; }
        public String getError() { return error; }
        public boolean hasError() { return error != null; }
        
        // Setters
        public void setTotalInResponse(int totalInResponse) { this.totalInResponse = totalInResponse; }
        public void setError(String error) { this.error = error; }
        
        // Incrementers
        public void incrementProcessed() { this.processed++; }
        public void incrementSaved() { this.saved++; }
        public void incrementSkipped() { this.skipped++; }
        public void incrementUpdated() { this.updated++; }
        public void incrementFailed() { this.failed++; }
        
        @Override
        public String toString() {
            return String.format("ArticleProcessingResult{total=%d, processed=%d, saved=%d, skipped=%d, updated=%d, failed=%d}", 
                totalInResponse, processed, saved, skipped, updated, failed);
        }
    }
}