package com.googlescholar.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity representing a Google Scholar search request and its metadata
 * This class maps to the search parameters and metadata from SerpApi response
 * 
 * @author Melany Rivera
 * @since October 2, 2025
 */
@Entity
@Table(name = "scholar_searches")
public class ScholarSearch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Search query is required")
    @Column(nullable = false, length = 500)
    private String query;

    @Column(name = "search_id", length = 100)
    private String searchId;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "total_results")
    private Long totalResults;

    @Column(name = "time_taken_displayed")
    private Double timeTakenDisplayed;

    @Column(name = "total_time_taken")
    private Double totalTimeTaken;

    // Advanced parameters
    @Column(name = "cites", length = 100)
    private String cites;

    @Column(name = "as_ylo")
    private Integer yearLow;

    @Column(name = "as_yhi")
    private Integer yearHigh;

    @Column(name = "scisbd")
    private Integer sortByDate;

    @Column(name = "cluster", length = 100)
    private String cluster;

    // Localization
    @Column(name = "hl", length = 10)
    private String language;

    @Column(name = "lr", length = 100)
    private String languageRestrictions;

    // Pagination
    @Column(name = "start_offset")
    private Integer startOffset;

    @Column(name = "num_results")
    private Integer numResults;

    // Search type and filters
    @Column(name = "as_sdt", length = 50)
    private String searchType;

    @Column(name = "safe", length = 20)
    private String safeSearch;

    @Column(name = "filter")
    private Integer filter;

    @Column(name = "as_vis")
    private Integer excludeCitations;

    @Column(name = "as_rr")
    private Integer reviewArticlesOnly;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @OneToMany(mappedBy = "searchQuery", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ScholarResult> results;

    /**
     * Default constructor
     * 
     * @author Melany Rivera
     * @since October 2, 2025
     */
    public ScholarSearch() {
        this.createdAt = LocalDateTime.now();
        this.status = "Processing";
    }

    /**
     * Constructor with query
     * 
     * @param query the search query
     * @author Melany Rivera
     * @since October 2, 2025
     */
    public ScholarSearch(String query) {
        this();
        this.query = query;
    }

    // Getters and Setters

    /**
     * Gets the unique identifier for this search
     * 
     * @return the search's unique ID
     * @author Melany Rivera
     * @since October 2, 2025
     */
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the search query string
     * 
     * @return the query used for searching
     * @author Melany Rivera
     * @since October 2, 2025
     */
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getSearchId() {
        return searchId;
    }

    public void setSearchId(String searchId) {
        this.searchId = searchId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Gets the total number of results found
     * 
     * @return the total result count
     * @author Melany Rivera
     * @since October 2, 2025
     */
    public Long getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(Long totalResults) {
        this.totalResults = totalResults;
    }

    public Double getTimeTakenDisplayed() {
        return timeTakenDisplayed;
    }

    public void setTimeTakenDisplayed(Double timeTakenDisplayed) {
        this.timeTakenDisplayed = timeTakenDisplayed;
    }

    public Double getTotalTimeTaken() {
        return totalTimeTaken;
    }

    public void setTotalTimeTaken(Double totalTimeTaken) {
        this.totalTimeTaken = totalTimeTaken;
    }

    public String getCites() {
        return cites;
    }

    public void setCites(String cites) {
        this.cites = cites;
    }

    public Integer getYearLow() {
        return yearLow;
    }

    public void setYearLow(Integer yearLow) {
        this.yearLow = yearLow;
    }

    public Integer getYearHigh() {
        return yearHigh;
    }

    public void setYearHigh(Integer yearHigh) {
        this.yearHigh = yearHigh;
    }

    public Integer getSortByDate() {
        return sortByDate;
    }

    public void setSortByDate(Integer sortByDate) {
        this.sortByDate = sortByDate;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getLanguageRestrictions() {
        return languageRestrictions;
    }

    public void setLanguageRestrictions(String languageRestrictions) {
        this.languageRestrictions = languageRestrictions;
    }

    public Integer getStartOffset() {
        return startOffset;
    }

    public void setStartOffset(Integer startOffset) {
        this.startOffset = startOffset;
    }

    public Integer getNumResults() {
        return numResults;
    }

    public void setNumResults(Integer numResults) {
        this.numResults = numResults;
    }

    public String getSearchType() {
        return searchType;
    }

    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    public String getSafeSearch() {
        return safeSearch;
    }

    public void setSafeSearch(String safeSearch) {
        this.safeSearch = safeSearch;
    }

    public Integer getFilter() {
        return filter;
    }

    public void setFilter(Integer filter) {
        this.filter = filter;
    }

    public Integer getExcludeCitations() {
        return excludeCitations;
    }

    public void setExcludeCitations(Integer excludeCitations) {
        this.excludeCitations = excludeCitations;
    }

    public Integer getReviewArticlesOnly() {
        return reviewArticlesOnly;
    }

    public void setReviewArticlesOnly(Integer reviewArticlesOnly) {
        this.reviewArticlesOnly = reviewArticlesOnly;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public List<ScholarResult> getResults() {
        return results;
    }

    public void setResults(List<ScholarResult> results) {
        this.results = results;
    }

    @Override
    public String toString() {
        return "ScholarSearch{" +
                "id=" + id +
                ", query='" + query + '\'' +
                ", status='" + status + '\'' +
                ", totalResults=" + totalResults +
                ", timeTaken=" + timeTakenDisplayed +
                '}';
    }
}