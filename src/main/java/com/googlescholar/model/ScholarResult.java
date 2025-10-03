package com.googlescholar.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity representing organic search results from Google Scholar API
 * This class maps to the organic_results structure from SerpApi Google Scholar response
 * 
 * @author Melany Rivera
 * @since October 2, 2025
 */
@Entity
@Table(name = "scholar_results")
public class ScholarResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "position")
    private Integer position;

    @NotBlank(message = "Title is required")
    @Column(nullable = false, length = 1000)
    private String title;

    @Column(name = "result_id", length = 100)
    private String resultId;

    @Column(length = 1000)
    private String link;

    @Column(length = 5000)
    private String snippet;

    @Column(name = "publication_summary", length = 1000)
    private String publicationSummary;

    @Column(name = "cited_by_total")
    private Integer citedByTotal;

    @Column(name = "cites_id", length = 100)
    private String citesId;

    @Column(name = "cluster_id", length = 100)
    private String clusterId;

    @Column(name = "versions_total")
    private Integer versionsTotal;

    @Column(name = "search_query", length = 500)
    private String searchQuery;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ElementCollection
    @CollectionTable(name = "scholar_result_resources", joinColumns = @JoinColumn(name = "result_id"))
    private List<ResourceLink> resources;

    /**
     * Default constructor
     * 
     * @author Melany Rivera
     * @since October 2, 2025
     */
    public ScholarResult() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Constructor with title and position
     * 
     * @param title the title of the scholarly work
     * @param position the position in search results
     * @author Melany Rivera
     * @since October 2, 2025
     */
    public ScholarResult(String title, Integer position) {
        this();
        this.title = title;
        this.position = position;
    }

    // Getters and Setters

    /**
     * Gets the unique identifier for this search result
     * 
     * @return the result's unique ID
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
     * Gets the position of this result in the search results
     * 
     * @return the position number (0-based)
     * @author Melany Rivera
     * @since October 2, 2025
     */
    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    /**
     * Gets the title of the scholarly work
     * 
     * @return the title of the publication
     * @author Melany Rivera
     * @since October 2, 2025
     */
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getResultId() {
        return resultId;
    }

    public void setResultId(String resultId) {
        this.resultId = resultId;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public String getPublicationSummary() {
        return publicationSummary;
    }

    public void setPublicationSummary(String publicationSummary) {
        this.publicationSummary = publicationSummary;
    }

    /**
     * Gets the total number of citations for this work
     * 
     * @return the total citation count
     * @author Melany Rivera
     * @since October 2, 2025
     */
    public Integer getCitedByTotal() {
        return citedByTotal;
    }

    public void setCitedByTotal(Integer citedByTotal) {
        this.citedByTotal = citedByTotal;
    }

    public String getCitesId() {
        return citesId;
    }

    public void setCitesId(String citesId) {
        this.citesId = citesId;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public Integer getVersionsTotal() {
        return versionsTotal;
    }

    public void setVersionsTotal(Integer versionsTotal) {
        this.versionsTotal = versionsTotal;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<ResourceLink> getResources() {
        return resources;
    }

    public void setResources(List<ResourceLink> resources) {
        this.resources = resources;
    }

    @Override
    public String toString() {
        return "ScholarResult{" +
                "id=" + id +
                ", position=" + position +
                ", title='" + title + '\'' +
                ", citedByTotal=" + citedByTotal +
                ", publicationSummary='" + publicationSummary + '\'' +
                '}';
    }

    /**
     * Embedded class for resource links (PDFs, etc.)
     */
    @Embeddable
    public static class ResourceLink {
        @Column(name = "title", length = 200)
        private String title;

        @Column(name = "file_format", length = 20)
        private String fileFormat;

        @Column(name = "link", length = 1000)
        private String link;

        // Constructors, getters and setters
        public ResourceLink() {}

        public ResourceLink(String title, String fileFormat, String link) {
            this.title = title;
            this.fileFormat = fileFormat;
            this.link = link;
        }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getFileFormat() { return fileFormat; }
        public void setFileFormat(String fileFormat) { this.fileFormat = fileFormat; }

        public String getLink() { return link; }
        public void setLink(String link) { this.link = link; }
    }
}