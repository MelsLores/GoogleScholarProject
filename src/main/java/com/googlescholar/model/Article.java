package com.googlescholar.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

/**
 * Entity representing articles in the database
 * Maps to the articles table structure
 * 
 * @author Melany Rivera
 * @since October 2, 2025
 */
@Entity
@Table(name = "articles")
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "authors", columnDefinition = "TEXT")
    private String authors;

    @Column(name = "publication_date")
    private LocalDate publicationDate;

    @Column(name = "abstract", columnDefinition = "TEXT")
    private String abstractText;

    @Column(name = "link", columnDefinition = "TEXT")
    private String link;

    @Column(name = "keywords", columnDefinition = "TEXT")
    private String keywords;

    @Column(name = "cited_by")
    private Integer citedBy = 0;

    // Foreign key to researcher - temporarily disabled
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "researcher_id")
    // private Researcher researcher;

    @Column(name = "citation_id", length = 100)
    private String citationId;

    @Column(name = "year")
    private Integer year;

    // Constructors
    public Article() {}

    public Article(String title, String authors, LocalDate publicationDate, 
                  String abstractText, String link, String keywords, Integer citedBy) {
        this.title = title;
        this.authors = authors;
        this.publicationDate = publicationDate;
        this.abstractText = abstractText;
        this.link = link;
        this.keywords = keywords;
        this.citedBy = citedBy != null ? citedBy : 0;
    }

    public Article(String title, String authors, LocalDate publicationDate, 
                  String abstractText, String link, String keywords, Integer citedBy,
                  Researcher researcher, String citationId, Integer year) {
        this(title, authors, publicationDate, abstractText, link, keywords, citedBy);
        // this.researcher = researcher; // temporarily disabled
        this.citationId = citationId;
        this.year = year;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthors() {
        return authors;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public LocalDate getPublicationDate() {
        return publicationDate;
    }

    public void setPublicationDate(LocalDate publicationDate) {
        this.publicationDate = publicationDate;
    }

    public String getAbstractText() {
        return abstractText;
    }

    public void setAbstractText(String abstractText) {
        this.abstractText = abstractText;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public Integer getCitedBy() {
        return citedBy;
    }

    public void setCitedBy(Integer citedBy) {
        this.citedBy = citedBy != null ? citedBy : 0;
    }

    // Temporarily disabled researcher methods
    /*
    public Researcher getResearcher() {
        return researcher;
    }

    public void setResearcher(Researcher researcher) {
        this.researcher = researcher;
    }
    */

    public String getCitationId() {
        return citationId;
    }

    public void setCitationId(String citationId) {
        this.citationId = citationId;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    @Override
    public String toString() {
        return "Article{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", authors='" + authors + '\'' +
                ", publicationDate=" + publicationDate +
                ", citedBy=" + citedBy +
                '}';
    }
}