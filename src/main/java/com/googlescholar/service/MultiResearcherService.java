package com.googlescholar.service;

import com.googlescholar.config.DatabaseManager;
import com.googlescholar.model.Article;
import com.googlescholar.model.Researcher;
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
 * Service for handling multiple researchers and their articles
 * Designed to process data from 2 researchers with 3 articles each (6 total)
 * 
 * @author Melany Rivera
 * @since October 2, 2025
 */
@Service
public class MultiResearcherService {
    
    private static final Logger LOGGER = Logger.getLogger(MultiResearcherService.class.getName());
    
    private final DatabaseManager databaseManager;
    private final ObjectMapper objectMapper;
    
    // Configuration for processing
    private static final int MAX_RESEARCHERS = 2;
    private static final int MAX_ARTICLES_PER_RESEARCHER = 3;
    
    public MultiResearcherService(DatabaseManager databaseManager, ObjectMapper objectMapper) {
        this.databaseManager = databaseManager;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Processes multiple researchers with their articles from SerpApi responses
     * Expected format: List of researcher data with their articles
     * 
     * @param researchersData List of researcher JSON responses
     * @return Processing result with statistics
     */
    public MultiResearcherResult processMultipleResearchers(List<ResearcherData> researchersData) {
        MultiResearcherResult result = new MultiResearcherResult();
        
        try {
            // Validate input
            if (researchersData == null || researchersData.isEmpty()) {
                result.setError("No researcher data provided");
                return result;
            }
            
            if (researchersData.size() > MAX_RESEARCHERS) {
                result.setError("Maximum " + MAX_RESEARCHERS + " researchers allowed");
                return result;
            }
            
            // Process each researcher
            for (ResearcherData researcherData : researchersData) {
                try {
                    ResearcherProcessingResult researcherResult = processResearcher(researcherData);
                    result.addResearcherResult(researcherResult);
                    
                    LOGGER.info("Processed researcher: " + researcherData.getAuthorId() + 
                              " with " + researcherResult.getArticlesSaved() + " articles");
                    
                } catch (Exception e) {
                    result.incrementFailedResearchers();
                    LOGGER.severe("Failed to process researcher " + researcherData.getAuthorId() + ": " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            result.setError("Error processing researchers: " + e.getMessage());
            LOGGER.severe("Error in processMultipleResearchers: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Processes a single researcher and their articles
     */
    private ResearcherProcessingResult processResearcher(ResearcherData researcherData) {
        ResearcherProcessingResult result = new ResearcherProcessingResult();
        result.setAuthorId(researcherData.getAuthorId());
        result.setName(researcherData.getName());
        
        try {
            // Check if researcher already exists
            Long researcherId = null;
            if (databaseManager.researcherExists(researcherData.getAuthorId())) {
                researcherId = databaseManager.getResearcherIdByAuthorId(researcherData.getAuthorId());
                result.setResearcherUpdated(true);
                LOGGER.info("Researcher already exists: " + researcherData.getAuthorId());
            } else {
                // Insert new researcher
                researcherId = databaseManager.insertResearcher(
                    researcherData.getAuthorId(),
                    researcherData.getName(),
                    researcherData.getAffiliation(),
                    researcherData.getEmail(),
                    researcherData.getHIndex(),
                    researcherData.getI10Index(),
                    researcherData.getTotalCitations(),
                    researcherData.getInterests(),
                    researcherData.getProfileUrl()
                );
                
                if (researcherId > 0) {
                    result.setResearcherSaved(true);
                    LOGGER.info("New researcher saved: " + researcherData.getAuthorId());
                } else {
                    result.setError("Failed to save researcher");
                    return result;
                }
            }
            
            // Process articles (limit to MAX_ARTICLES_PER_RESEARCHER)
            if (researcherData.getArticlesJson() != null) {
                processResearcherArticles(researcherData.getArticlesJson(), researcherId, result);
            }
            
        } catch (Exception e) {
            result.setError("Error processing researcher: " + e.getMessage());
            LOGGER.severe("Error processing researcher " + researcherData.getAuthorId() + ": " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * Processes articles for a specific researcher
     */
    private void processResearcherArticles(String articlesJson, Long researcherId, ResearcherProcessingResult result) {
        try {
            JsonNode rootNode = objectMapper.readTree(articlesJson);
            JsonNode articlesNode = rootNode.get("articles");
            
            if (articlesNode == null || !articlesNode.isArray()) {
                result.setError("Invalid articles JSON format");
                return;
            }
            
            int processedCount = 0;
            for (JsonNode articleNode : articlesNode) {
                // Limit articles per researcher
                if (processedCount >= MAX_ARTICLES_PER_RESEARCHER) {
                    LOGGER.info("Reached maximum articles limit (" + MAX_ARTICLES_PER_RESEARCHER + ") for researcher");
                    break;
                }
                
                try {
                    Article article = parseArticleFromJson(articleNode, researcherId);
                    if (article != null && article.getTitle() != null && !article.getTitle().trim().isEmpty()) {
                        
                        // Check if article already exists
                        if (!databaseManager.articleExists(article.getTitle())) {
                            long articleId = databaseManager.insertArticleWithResearcher(
                                article.getTitle(),
                                article.getAuthors(),
                                article.getPublicationDate(),
                                article.getAbstractText(),
                                article.getLink(),
                                article.getKeywords(),
                                article.getCitedBy() != null ? article.getCitedBy() : 0,
                                researcherId,
                                article.getCitationId(),
                                article.getYear()
                            );
                            
                            if (articleId > 0) {
                                result.incrementArticlesSaved();
                                LOGGER.info("Article saved: " + article.getTitle());
                            } else {
                                result.incrementArticlesFailed();
                            }
                        } else {
                            // Update citation count if article exists
                            if (article.getCitedBy() != null) {
                                boolean updated = databaseManager.updateCitationCount(article.getTitle(), article.getCitedBy());
                                if (updated) {
                                    result.incrementArticlesUpdated();
                                }
                            }
                            result.incrementArticlesSkipped();
                        }
                        result.incrementArticlesProcessed();
                        processedCount++;
                    }
                } catch (Exception e) {
                    result.incrementArticlesFailed();
                    LOGGER.warning("Error processing article: " + e.getMessage());
                }
            }
            
            result.setTotalArticlesInResponse(articlesNode.size());
            
        } catch (Exception e) {
            result.setError("Error processing articles JSON: " + e.getMessage());
            LOGGER.severe("Error processing articles: " + e.getMessage());
        }
    }
    
    /**
     * Parses a single article from JSON node with researcher association
     */
    private Article parseArticleFromJson(JsonNode articleNode, Long researcherId) {
        try {
            String title = getJsonValue(articleNode, "title");
            String authors = getJsonValue(articleNode, "authors");
            String link = getJsonValue(articleNode, "link");
            String publication = getJsonValue(articleNode, "publication");
            String citationId = getJsonValue(articleNode, "citation_id");
            
            // Extract year from publication string or from year field
            LocalDate publicationDate = extractDateFromPublication(publication);
            Integer year = extractYearFromPublication(publication);
            
            // Try to get year from direct field if available
            JsonNode yearNode = articleNode.get("year");
            if (yearNode != null && !yearNode.isNull()) {
                try {
                    year = Integer.parseInt(yearNode.asText());
                    if (publicationDate == null && year != null) {
                        publicationDate = LocalDate.of(year, 1, 1);
                    }
                } catch (NumberFormatException e) {
                    // Keep existing year value
                }
            }
            
            // Get cited_by count
            Integer citedBy = 0;
            JsonNode citedByNode = articleNode.get("cited_by");
            if (citedByNode != null && citedByNode.has("value")) {
                citedBy = citedByNode.get("value").asInt(0);
            }
            
            // Extract keywords from publication
            String keywords = extractKeywordsFromPublication(publication);
            
            Article article = new Article();
            article.setTitle(title);
            article.setAuthors(authors);
            article.setPublicationDate(publicationDate);
            article.setAbstractText(null); // Not available in Author Articles API
            article.setLink(link);
            article.setKeywords(keywords);
            article.setCitedBy(citedBy);
            article.setCitationId(citationId);
            article.setYear(year);
            
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
     */
    private LocalDate extractDateFromPublication(String publication) {
        Integer year = extractYearFromPublication(publication);
        return year != null ? LocalDate.of(year, 1, 1) : null;
    }
    
    /**
     * Extracts year from publication string
     */
    private Integer extractYearFromPublication(String publication) {
        if (publication == null || publication.trim().isEmpty()) {
            return null;
        }
        
        try {
            Pattern yearPattern = Pattern.compile("\\b(19|20)\\d{2}\\b");
            Matcher matcher = yearPattern.matcher(publication);
            
            if (matcher.find()) {
                return Integer.parseInt(matcher.group());
            }
        } catch (Exception e) {
            LOGGER.warning("Error extracting year from publication: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Extracts keywords from publication string
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
                Integer year = extractYearFromPublication(publication);
                
                if (year != null) {
                    return journalName + ", " + year;
                }
                
                return journalName;
            }
        } catch (Exception e) {
            LOGGER.warning("Error extracting keywords from publication: " + e.getMessage());
        }
        
        return null;
    }
    
    // Data classes
    public static class ResearcherData {
        private String authorId;
        private String name;
        private String affiliation;
        private String email;
        private Integer hIndex;
        private Integer i10Index;
        private Integer totalCitations;
        private String interests;
        private String profileUrl;
        private String articlesJson;
        
        // Constructors
        public ResearcherData() {}
        
        public ResearcherData(String authorId, String name, String articlesJson) {
            this.authorId = authorId;
            this.name = name;
            this.articlesJson = articlesJson;
        }
        
        // Getters and setters
        public String getAuthorId() { return authorId; }
        public void setAuthorId(String authorId) { this.authorId = authorId; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getAffiliation() { return affiliation; }
        public void setAffiliation(String affiliation) { this.affiliation = affiliation; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public Integer getHIndex() { return hIndex; }
        public void setHIndex(Integer hIndex) { this.hIndex = hIndex; }
        
        public Integer getI10Index() { return i10Index; }
        public void setI10Index(Integer i10Index) { this.i10Index = i10Index; }
        
        public Integer getTotalCitations() { return totalCitations; }
        public void setTotalCitations(Integer totalCitations) { this.totalCitations = totalCitations; }
        
        public String getInterests() { return interests; }
        public void setInterests(String interests) { this.interests = interests; }
        
        public String getProfileUrl() { return profileUrl; }
        public void setProfileUrl(String profileUrl) { this.profileUrl = profileUrl; }
        
        public String getArticlesJson() { return articlesJson; }
        public void setArticlesJson(String articlesJson) { this.articlesJson = articlesJson; }
    }
    
    public static class ResearcherProcessingResult {
        private String authorId;
        private String name;
        private boolean researcherSaved = false;
        private boolean researcherUpdated = false;
        private int totalArticlesInResponse = 0;
        private int articlesProcessed = 0;
        private int articlesSaved = 0;
        private int articlesSkipped = 0;
        private int articlesUpdated = 0;
        private int articlesFailed = 0;
        private String error = null;
        
        // Getters and setters
        public String getAuthorId() { return authorId; }
        public void setAuthorId(String authorId) { this.authorId = authorId; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public boolean isResearcherSaved() { return researcherSaved; }
        public void setResearcherSaved(boolean researcherSaved) { this.researcherSaved = researcherSaved; }
        
        public boolean isResearcherUpdated() { return researcherUpdated; }
        public void setResearcherUpdated(boolean researcherUpdated) { this.researcherUpdated = researcherUpdated; }
        
        public int getTotalArticlesInResponse() { return totalArticlesInResponse; }
        public void setTotalArticlesInResponse(int totalArticlesInResponse) { this.totalArticlesInResponse = totalArticlesInResponse; }
        
        public int getArticlesProcessed() { return articlesProcessed; }
        public void incrementArticlesProcessed() { this.articlesProcessed++; }
        
        public int getArticlesSaved() { return articlesSaved; }
        public void incrementArticlesSaved() { this.articlesSaved++; }
        
        public int getArticlesSkipped() { return articlesSkipped; }
        public void incrementArticlesSkipped() { this.articlesSkipped++; }
        
        public int getArticlesUpdated() { return articlesUpdated; }
        public void incrementArticlesUpdated() { this.articlesUpdated++; }
        
        public int getArticlesFailed() { return articlesFailed; }
        public void incrementArticlesFailed() { this.articlesFailed++; }
        
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        public boolean hasError() { return error != null; }
    }
    
    public static class MultiResearcherResult {
        private List<ResearcherProcessingResult> researcherResults = new ArrayList<>();
        private int failedResearchers = 0;
        private String error = null;
        
        public void addResearcherResult(ResearcherProcessingResult result) {
            this.researcherResults.add(result);
        }
        
        public List<ResearcherProcessingResult> getResearcherResults() { return researcherResults; }
        
        public int getFailedResearchers() { return failedResearchers; }
        public void incrementFailedResearchers() { this.failedResearchers++; }
        
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        public boolean hasError() { return error != null; }
        
        // Summary methods
        public int getTotalResearchers() { return researcherResults.size() + failedResearchers; }
        public int getSuccessfulResearchers() { return researcherResults.size(); }
        
        public int getTotalArticlesSaved() {
            return researcherResults.stream().mapToInt(ResearcherProcessingResult::getArticlesSaved).sum();
        }
        
        public int getTotalArticlesProcessed() {
            return researcherResults.stream().mapToInt(ResearcherProcessingResult::getArticlesProcessed).sum();
        }
        
        public int getTotalArticlesSkipped() {
            return researcherResults.stream().mapToInt(ResearcherProcessingResult::getArticlesSkipped).sum();
        }
        
        public int getTotalArticlesUpdated() {
            return researcherResults.stream().mapToInt(ResearcherProcessingResult::getArticlesUpdated).sum();
        }
    }
}