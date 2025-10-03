package com.googlescholar.view;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for Google Scholar search responses
 * This class represents the response structure from the SerpApi Google Scholar API
 * 
 * @author Melany Rivera
 * @since October 2, 2025
 */
public class ScholarSearchResponseDTO {

    private SearchMetadata searchMetadata;
    private SearchParameters searchParameters;
    private SearchInformation searchInformation;
    private List<OrganicResult> organicResults;
    private List<RelatedSearch> relatedSearches;
    private Pagination pagination;

    /**
     * Default constructor
     * 
     * @author Melany Rivera
     * @since October 2, 2025
     */
    public ScholarSearchResponseDTO() {
    }

    // Getters and Setters

    public SearchMetadata getSearchMetadata() {
        return searchMetadata;
    }

    public void setSearchMetadata(SearchMetadata searchMetadata) {
        this.searchMetadata = searchMetadata;
    }

    public SearchParameters getSearchParameters() {
        return searchParameters;
    }

    public void setSearchParameters(SearchParameters searchParameters) {
        this.searchParameters = searchParameters;
    }

    public SearchInformation getSearchInformation() {
        return searchInformation;
    }

    public void setSearchInformation(SearchInformation searchInformation) {
        this.searchInformation = searchInformation;
    }

    public List<OrganicResult> getOrganicResults() {
        return organicResults;
    }

    public void setOrganicResults(List<OrganicResult> organicResults) {
        this.organicResults = organicResults;
    }

    public List<RelatedSearch> getRelatedSearches() {
        return relatedSearches;
    }

    public void setRelatedSearches(List<RelatedSearch> relatedSearches) {
        this.relatedSearches = relatedSearches;
    }

    public Pagination getPagination() {
        return pagination;
    }

    public void setPagination(Pagination pagination) {
        this.pagination = pagination;
    }

    /**
     * Nested class for search metadata
     * 
     * @author Melany Rivera
     * @since October 2, 2025
     */
    public static class SearchMetadata {
        private String id;
        private String status;
        private String jsonEndpoint;
        private LocalDateTime createdAt;
        private LocalDateTime processedAt;
        private String googleScholarUrl;
        private String rawHtmlFile;
        private Double totalTimeTaken;

        // Getters and Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getJsonEndpoint() { return jsonEndpoint; }
        public void setJsonEndpoint(String jsonEndpoint) { this.jsonEndpoint = jsonEndpoint; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public LocalDateTime getProcessedAt() { return processedAt; }
        public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

        public String getGoogleScholarUrl() { return googleScholarUrl; }
        public void setGoogleScholarUrl(String googleScholarUrl) { this.googleScholarUrl = googleScholarUrl; }

        public String getRawHtmlFile() { return rawHtmlFile; }
        public void setRawHtmlFile(String rawHtmlFile) { this.rawHtmlFile = rawHtmlFile; }

        public Double getTotalTimeTaken() { return totalTimeTaken; }
        public void setTotalTimeTaken(Double totalTimeTaken) { this.totalTimeTaken = totalTimeTaken; }
    }

    /**
     * Nested class for search parameters
     * 
     * @author Melany Rivera
     * @since October 2, 2025
     */
    public static class SearchParameters {
        private String engine;
        private String q;

        // Getters and Setters
        public String getEngine() { return engine; }
        public void setEngine(String engine) { this.engine = engine; }

        public String getQ() { return q; }
        public void setQ(String q) { this.q = q; }
    }

    /**
     * Nested class for search information
     * 
     * @author Melany Rivera
     * @since October 2, 2025
     */
    public static class SearchInformation {
        private Long totalResults;
        private Double timeTakenDisplayed;
        private String queryDisplayed;

        // Getters and Setters
        public Long getTotalResults() { return totalResults; }
        public void setTotalResults(Long totalResults) { this.totalResults = totalResults; }

        public Double getTimeTakenDisplayed() { return timeTakenDisplayed; }
        public void setTimeTakenDisplayed(Double timeTakenDisplayed) { this.timeTakenDisplayed = timeTakenDisplayed; }

        public String getQueryDisplayed() { return queryDisplayed; }
        public void setQueryDisplayed(String queryDisplayed) { this.queryDisplayed = queryDisplayed; }
    }

    /**
     * Nested class for organic results
     * 
     * @author Melany Rivera
     * @since October 2, 2025
     */
    public static class OrganicResult {
        private Integer position;
        private String title;
        private String resultId;
        private String link;
        private String snippet;
        private PublicationInfo publicationInfo;
        private List<Resource> resources;
        private InlineLinks inlineLinks;

        // Getters and Setters
        public Integer getPosition() { return position; }
        public void setPosition(Integer position) { this.position = position; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getResultId() { return resultId; }
        public void setResultId(String resultId) { this.resultId = resultId; }

        public String getLink() { return link; }
        public void setLink(String link) { this.link = link; }

        public String getSnippet() { return snippet; }
        public void setSnippet(String snippet) { this.snippet = snippet; }

        public PublicationInfo getPublicationInfo() { return publicationInfo; }
        public void setPublicationInfo(PublicationInfo publicationInfo) { this.publicationInfo = publicationInfo; }

        public List<Resource> getResources() { return resources; }
        public void setResources(List<Resource> resources) { this.resources = resources; }

        public InlineLinks getInlineLinks() { return inlineLinks; }
        public void setInlineLinks(InlineLinks inlineLinks) { this.inlineLinks = inlineLinks; }

        /**
         * Nested class for publication information
         */
        public static class PublicationInfo {
            private String summary;

            public String getSummary() { return summary; }
            public void setSummary(String summary) { this.summary = summary; }
        }

        /**
         * Nested class for resources (PDFs, etc.)
         */
        public static class Resource {
            private String title;
            private String fileFormat;
            private String link;

            public String getTitle() { return title; }
            public void setTitle(String title) { this.title = title; }

            public String getFileFormat() { return fileFormat; }
            public void setFileFormat(String fileFormat) { this.fileFormat = fileFormat; }

            public String getLink() { return link; }
            public void setLink(String link) { this.link = link; }
        }

        /**
         * Nested class for inline links (citations, versions, etc.)
         */
        public static class InlineLinks {
            private String serpApiCiteLink;
            private CitedBy citedBy;
            private String relatedPagesLink;
            private Versions versions;
            private String cachedPageLink;

            public String getSerpApiCiteLink() { return serpApiCiteLink; }
            public void setSerpApiCiteLink(String serpApiCiteLink) { this.serpApiCiteLink = serpApiCiteLink; }

            public CitedBy getCitedBy() { return citedBy; }
            public void setCitedBy(CitedBy citedBy) { this.citedBy = citedBy; }

            public String getRelatedPagesLink() { return relatedPagesLink; }
            public void setRelatedPagesLink(String relatedPagesLink) { this.relatedPagesLink = relatedPagesLink; }

            public Versions getVersions() { return versions; }
            public void setVersions(Versions versions) { this.versions = versions; }

            public String getCachedPageLink() { return cachedPageLink; }
            public void setCachedPageLink(String cachedPageLink) { this.cachedPageLink = cachedPageLink; }

            /**
             * Nested class for cited by information
             */
            public static class CitedBy {
                private Integer total;
                private String link;
                private String citesId;
                private String serpApiScholarLink;

                public Integer getTotal() { return total; }
                public void setTotal(Integer total) { this.total = total; }

                public String getLink() { return link; }
                public void setLink(String link) { this.link = link; }

                public String getCitesId() { return citesId; }
                public void setCitesId(String citesId) { this.citesId = citesId; }

                public String getSerpApiScholarLink() { return serpApiScholarLink; }
                public void setSerpApiScholarLink(String serpApiScholarLink) { this.serpApiScholarLink = serpApiScholarLink; }
            }

            /**
             * Nested class for versions information
             */
            public static class Versions {
                private Integer total;
                private String link;
                private String clusterId;
                private String serpApiScholarLink;

                public Integer getTotal() { return total; }
                public void setTotal(Integer total) { this.total = total; }

                public String getLink() { return link; }
                public void setLink(String link) { this.link = link; }

                public String getClusterId() { return clusterId; }
                public void setClusterId(String clusterId) { this.clusterId = clusterId; }

                public String getSerpApiScholarLink() { return serpApiScholarLink; }
                public void setSerpApiScholarLink(String serpApiScholarLink) { this.serpApiScholarLink = serpApiScholarLink; }
            }
        }
    }

    /**
     * Nested class for related searches
     * 
     * @author Melany Rivera
     * @since October 2, 2025
     */
    public static class RelatedSearch {
        private String query;
        private String link;

        public String getQuery() { return query; }
        public void setQuery(String query) { this.query = query; }

        public String getLink() { return link; }
        public void setLink(String link) { this.link = link; }
    }

    /**
     * Nested class for pagination
     * 
     * @author Melany Rivera
     * @since October 2, 2025
     */
    public static class Pagination {
        private Integer current;
        private String next;
        private Object otherPages;

        public Integer getCurrent() { return current; }
        public void setCurrent(Integer current) { this.current = current; }

        public String getNext() { return next; }
        public void setNext(String next) { this.next = next; }

        public Object getOtherPages() { return otherPages; }
        public void setOtherPages(Object otherPages) { this.otherPages = otherPages; }
    }

    @Override
    public String toString() {
        return "ScholarSearchResponseDTO{" +
                "totalResults=" + (searchInformation != null ? searchInformation.getTotalResults() : "N/A") +
                ", resultsCount=" + (organicResults != null ? organicResults.size() : 0) +
                ", status=" + (searchMetadata != null ? searchMetadata.getStatus() : "N/A") +
                '}';
    }
}