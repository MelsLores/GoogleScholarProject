package com.googlescholar.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Entity representing an author from Google Scholar
 * This class contains comprehensive information about academic authors including
 * their citation metrics, research interests, and profile details.
 * 
 * @author Melany Rivera
 * @since October 2, 2025
 */
@Entity
@Table(name = "authors")
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Author name is required")
    @Column(nullable = false, length = 200)
    private String name;

    @Email(message = "Email should be valid")
    @Column(length = 150)
    private String email;

    @Column(length = 300)
    private String affiliation;

    @Column(name = "total_citations")
    private Integer totalCitations;

    @Column(name = "h_index")
    private Integer hIndex;

    @Column(name = "i10_index")
    private Integer i10Index;

    @Column(name = "scholar_profile_url", length = 500)
    private String scholarProfileUrl;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(name = "research_interests", length = 1000)
    private String researchInterests;

    @Column(name = "domains", length = 500)
    private String domains;

    @Column(name = "verified")
    private Boolean verified;

    @Column(name = "citation_count_2018")
    private Integer citationCount2018;

    @Column(name = "citation_count_2019")
    private Integer citationCount2019;

    @Column(name = "citation_count_2020")
    private Integer citationCount2020;

    @Column(name = "citation_count_2021")
    private Integer citationCount2021;

    @Column(name = "citation_count_2022")
    private Integer citationCount2022;

    @Column(name = "citation_count_2023")
    private Integer citationCount2023;

    /**
     * Default constructor
     * Initializes an Author with default values for citation metrics
     * 
     * @author Melany Rivera
     * @since October 2, 2025
     */
    public Author() {
        this.verified = false;
        this.totalCitations = 0;
        this.hIndex = 0;
        this.i10Index = 0;
    }

    /**
     * Constructor with author name
     * Creates an Author instance with the specified name
     * 
     * @param name the author's full name
     * @author Melany Rivera
     * @since October 2, 2025
     */
    public Author(String name) {
        this();
        this.name = name;
    }

    /**
     * Constructor with name and institutional affiliation
     * Creates an Author instance with name and affiliation
     * 
     * @param name the author's full name
     * @param affiliation the author's institutional affiliation
     * @author Melany Rivera
     * @since October 2, 2025
     */
    public Author(String name, String affiliation) {
        this(name);
        this.affiliation = affiliation;
    }

    // Getters and Setters

    /**
     * Gets the unique identifier for this author
     * 
     * @return the author's unique ID
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
     * Gets the author's full name
     * 
     * @return the author's complete name
     * @author Melany Rivera
     * @since October 2, 2025
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the author's email address
     * 
     * @return the author's email address
     * @author Melany Rivera
     * @since October 2, 2025
     */
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the author's institutional affiliation
     * 
     * @return the author's current institution or organization
     * @author Melany Rivera
     * @since October 2, 2025
     */
    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    /**
     * Gets the total number of citations for this author
     * 
     * @return the total citation count across all publications
     * @author Melany Rivera
     * @since October 2, 2025
     */
    public Integer getTotalCitations() {
        return totalCitations;
    }

    public void setTotalCitations(Integer totalCitations) {
        this.totalCitations = totalCitations;
    }

    /**
     * Gets the author's H-Index
     * The H-Index is a metric that measures both productivity and citation impact
     * 
     * @return the author's H-Index value
     * @author Melany Rivera
     * @since October 2, 2025
     */
    public Integer getHIndex() {
        return hIndex;
    }

    public void setHIndex(Integer hIndex) {
        this.hIndex = hIndex;
    }

    /**
     * Gets the author's i10-Index
     * The i10-Index is the number of publications with at least 10 citations
     * 
     * @return the author's i10-Index value
     * @author Melany Rivera
     * @since October 2, 2025
     */
    public Integer getI10Index() {
        return i10Index;
    }

    public void setI10Index(Integer i10Index) {
        this.i10Index = i10Index;
    }

    public String getScholarProfileUrl() {
        return scholarProfileUrl;
    }

    public void setScholarProfileUrl(String scholarProfileUrl) {
        this.scholarProfileUrl = scholarProfileUrl;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getResearchInterests() {
        return researchInterests;
    }

    public void setResearchInterests(String researchInterests) {
        this.researchInterests = researchInterests;
    }

    public String getDomains() {
        return domains;
    }

    public void setDomains(String domains) {
        this.domains = domains;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    public Integer getCitationCount2018() {
        return citationCount2018;
    }

    public void setCitationCount2018(Integer citationCount2018) {
        this.citationCount2018 = citationCount2018;
    }

    public Integer getCitationCount2019() {
        return citationCount2019;
    }

    public void setCitationCount2019(Integer citationCount2019) {
        this.citationCount2019 = citationCount2019;
    }

    public Integer getCitationCount2020() {
        return citationCount2020;
    }

    public void setCitationCount2020(Integer citationCount2020) {
        this.citationCount2020 = citationCount2020;
    }

    public Integer getCitationCount2021() {
        return citationCount2021;
    }

    public void setCitationCount2021(Integer citationCount2021) {
        this.citationCount2021 = citationCount2021;
    }

    public Integer getCitationCount2022() {
        return citationCount2022;
    }

    public void setCitationCount2022(Integer citationCount2022) {
        this.citationCount2022 = citationCount2022;
    }

    public Integer getCitationCount2023() {
        return citationCount2023;
    }

    public void setCitationCount2023(Integer citationCount2023) {
        this.citationCount2023 = citationCount2023;
    }

    @Override
    public String toString() {
        return "Author{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", affiliation='" + affiliation + '\'' +
                ", totalCitations=" + totalCitations +
                ", hIndex=" + hIndex +
                ", i10Index=" + i10Index +
                ", verified=" + verified +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Author author = (Author) o;
        
        if (id != null ? !id.equals(author.id) : author.id != null) return false;
        return name != null ? name.equals(author.name) : author.name == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}