package com.googlescholar.view;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * Data Transfer Object for Google Scholar search requests
 * This class represents the request parameters for the SerpApi Google Scholar API
 * 
 * @author Melany Rivera
 * @since October 2, 2025
 */
public class ScholarSearchRequestDTO {

    @NotBlank(message = "Search query is required")
    private String q;

    // Advanced Google Scholar Parameters
    private String cites;

    @Min(value = 1900, message = "Year from must be after 1900")
    private Integer asYlo;

    @Max(value = 2030, message = "Year to must be before 2030")
    private Integer asYhi;

    private Integer scisbd; // 0=relevance, 1=abstracts, 2=everything

    private String cluster;

    // Localization
    private String hl = "en"; // Language (default English)
    private String lr; // Language restrictions

    // Pagination
    @Min(value = 0, message = "Page start offset must be non-negative")
    private Integer pageStart = 0;

    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 20, message = "Page size cannot exceed 20")
    private Integer pageSize = 10;

    // Search Type and Filters
    private String asSdt; // Search type/filter
    private String safe = "off"; // Safe search
    private Integer filter = 1; // Similar/omitted results filter
    private Integer asVis = 0; // Include citations (0=include, 1=exclude)
    private Integer asRr = 0; // Review articles only (0=all, 1=reviews only)

    // SerpApi Parameters
    private String engine = "google_scholar";
    private Boolean noCache = false;
    private Boolean async = false;
    private String output = "json";

    /**
     * Default constructor
     * 
     * @author Melany Rivera
     * @since October 2, 2025
     */
    public ScholarSearchRequestDTO() {
    }

    /**
     * Constructor with query
     * 
     * @param q the search query
     * @author Melany Rivera
     * @since October 2, 2025
     */
    public ScholarSearchRequestDTO(String q) {
        this.q = q;
    }

    // Getters and Setters

    /**
     * Gets the search query
     * 
     * @return the search query string
     * @author Melany Rivera
     * @since October 2, 2025
     */
    public String getQ() {
        return q;
    }

    public void setQ(String q) {
        this.q = q;
    }

    public String getCites() {
        return cites;
    }

    public void setCites(String cites) {
        this.cites = cites;
    }

    /**
     * Gets the year from parameter
     * 
     * @return the starting year for results
     * @author Melany Rivera
     * @since October 2, 2025
     */
    public Integer getAsYlo() {
        return asYlo;
    }

    public void setAsYlo(Integer asYlo) {
        this.asYlo = asYlo;
    }

    /**
     * Gets the year to parameter
     * 
     * @return the ending year for results
     * @author Melany Rivera
     * @since October 2, 2025
     */
    public Integer getAsYhi() {
        return asYhi;
    }

    public void setAsYhi(Integer asYhi) {
        this.asYhi = asYhi;
    }

    public Integer getScisbd() {
        return scisbd;
    }

    public void setScisbd(Integer scisbd) {
        this.scisbd = scisbd;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public String getHl() {
        return hl;
    }

    public void setHl(String hl) {
        this.hl = hl;
    }

    public String getLr() {
        return lr;
    }

    public void setLr(String lr) {
        this.lr = lr;
    }

    /**
     * Gets the pagination start offset
     * 
     * @return the number of results to skip
     * @author Melany Rivera
     * @since October 2, 2025
     */
    public Integer getPageStart() {
        return pageStart;
    }

    public void setPageStart(Integer pageStart) {
        this.pageStart = pageStart;
    }

    /**
     * Gets the page size for pagination
     * 
     * @return the maximum number of results per page
     * @author Melany Rivera
     * @since October 2, 2025
     */
    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getAsSdt() {
        return asSdt;
    }

    public void setAsSdt(String asSdt) {
        this.asSdt = asSdt;
    }

    public String getSafe() {
        return safe;
    }

    public void setSafe(String safe) {
        this.safe = safe;
    }

    public Integer getFilter() {
        return filter;
    }

    public void setFilter(Integer filter) {
        this.filter = filter;
    }

    public Integer getAsVis() {
        return asVis;
    }

    public void setAsVis(Integer asVis) {
        this.asVis = asVis;
    }

    public Integer getAsRr() {
        return asRr;
    }

    public void setAsRr(Integer asRr) {
        this.asRr = asRr;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public Boolean getNoCache() {
        return noCache;
    }

    public void setNoCache(Boolean noCache) {
        this.noCache = noCache;
    }

    public Boolean getAsync() {
        return async;
    }

    public void setAsync(Boolean async) {
        this.async = async;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    @Override
    public String toString() {
        return "ScholarSearchRequestDTO{" +
                "q='" + q + '\'' +
                ", asYlo=" + asYlo +
                ", asYhi=" + asYhi +
                ", pageStart=" + pageStart +
                ", pageSize=" + pageSize +
                ", hl='" + hl + '\'' +
                '}';
    }
}