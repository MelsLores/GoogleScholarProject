package com.googlescholar.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing researchers/authors in the database
 * Maps to the researchers table structure
 * 
 * @author Melany Rivera
 * @since October 2, 2025
 */
@Entity
@Table(name = "researchers")
public class Researcher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Author ID is required")
    @Column(name = "author_id", nullable = false, unique = true, length = 50)
    private String authorId;

    @NotBlank(message = "Name is required")
    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "affiliation", length = 500)
    private String affiliation;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "h_index")
    private Integer hIndex;

    @Column(name = "i10_index")
    private Integer i10Index;

    @Column(name = "total_citations")
    private Integer totalCitations;

    @Column(name = "interests", columnDefinition = "TEXT")
    private String interests;

    @Column(name = "profile_url", columnDefinition = "TEXT")
    private String profileUrl;

    // Relationship with articles - temporarily disabled
    // @OneToMany(mappedBy = "researcher", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private List<Article> articles = new ArrayList<>();

    // Constructors
    public Researcher() {}

    public Researcher(String authorId, String name, String affiliation, String email, 
                     Integer hIndex, Integer i10Index, Integer totalCitations, 
                     String interests, String profileUrl) {
        this.authorId = authorId;
        this.name = name;
        this.affiliation = affiliation;
        this.email = email;
        this.hIndex = hIndex;
        this.i10Index = i10Index;
        this.totalCitations = totalCitations;
        this.interests = interests;
        this.profileUrl = profileUrl;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getHIndex() {
        return hIndex;
    }

    public void setHIndex(Integer hIndex) {
        this.hIndex = hIndex;
    }

    public Integer getI10Index() {
        return i10Index;
    }

    public void setI10Index(Integer i10Index) {
        this.i10Index = i10Index;
    }

    public Integer getTotalCitations() {
        return totalCitations;
    }

    public void setTotalCitations(Integer totalCitations) {
        this.totalCitations = totalCitations;
    }

    public String getInterests() {
        return interests;
    }

    public void setInterests(String interests) {
        this.interests = interests;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    // Articles methods - temporarily disabled
    /*
    public List<Article> getArticles() {
        return articles;
    }

    public void setArticles(List<Article> articles) {
        this.articles = articles;
    }

    // Helper methods
    public void addArticle(Article article) {
        articles.add(article);
        article.setResearcher(this);
    }

    public void removeArticle(Article article) {
        articles.remove(article);
        article.setResearcher(null);
    }
    */

    @Override
    public String toString() {
        return "Researcher{" +
                "id=" + id +
                ", authorId='" + authorId + '\'' +
                ", name='" + name + '\'' +
                ", affiliation='" + affiliation + '\'' +
                ", hIndex=" + hIndex +
                ", totalCitations=" + totalCitations +
                '}';
    }
}