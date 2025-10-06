package com.googlescholar.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Database Manager for SQL Server connections using JDBC
 * Handles database connections and operations for the Google Scholar Project
 * 
 * @author Melany Rivera
 * @since October 2, 2025
 */
@Component
public class DatabaseManager {
    
    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
    
    @Value("${spring.datasource.url}")
    private String jdbcUrl;
    
    @Value("${spring.datasource.username:}")
    private String username;
    
    @Value("${spring.datasource.password:}")
    private String password;
    
    @Value("${spring.datasource.driver-class-name:com.microsoft.sqlserver.jdbc.SQLServerDriver}")
    private String driverClassName;
    
    /**
     * Establishes a connection to the SQL Server database
     * 
     * @return Connection object to the database
     * @throws SQLException if connection fails
     */
    public Connection getConnection() throws SQLException {
        try {
            // Load the SQL Server JDBC driver
            Class.forName(driverClassName);
            
            // Create connection - handle Windows Authentication
            Connection connection;
            if (username == null || username.trim().isEmpty()) {
                // Windows Authentication - use URL only
                LOGGER.info("Connecting with Windows Authentication to: " + jdbcUrl);
                connection = DriverManager.getConnection(jdbcUrl);
            } else {
                // SQL Server Authentication - use username/password
                LOGGER.info("Connecting with SQL Server Authentication to: " + jdbcUrl + " as user: " + username);
                connection = DriverManager.getConnection(jdbcUrl, username, password);
            }
            
            LOGGER.info("Successfully connected to SQL Server database");
            return connection;
            
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "SQL Server JDBC Driver not found", e);
            throw new SQLException("SQL Server JDBC Driver not found", e);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to connect to database: " + e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Tests the database connection
     * 
     * @return true if connection is successful, false otherwise
     */
    public boolean testConnection() {
        try (Connection connection = getConnection()) {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Database connection test failed: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Inserts an article into the articles table with researcher association
     * 
     * @param title Article title
     * @param authors Article authors (comma-separated)
     * @param publicationDate Publication date
     * @param abstractText Article abstract
     * @param link Article link
     * @param keywords Article keywords (comma-separated)
     * @param citedBy Number of citations
     * @param researcherId ID of the associated researcher
     * @param citationId Citation ID from Google Scholar
     * @param year Publication year
     * @return Generated article ID, or -1 if insertion failed
     */
    public long insertArticleWithResearcher(String title, String authors, LocalDate publicationDate, 
                                          String abstractText, String link, String keywords, int citedBy,
                                          Long researcherId, String citationId, Integer year) {
        
        String insertSQL = """
            INSERT INTO articles (title, authors, publication_date, abstract, link, keywords, cited_by, researcher_id, citation_id, year)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {
            
            // Set parameters
            statement.setString(1, title);
            statement.setString(2, authors);
            statement.setDate(3, publicationDate != null ? Date.valueOf(publicationDate) : null);
            statement.setString(4, abstractText);
            statement.setString(5, link);
            statement.setString(6, keywords);
            statement.setInt(7, citedBy);
            statement.setObject(8, researcherId);
            statement.setString(9, citationId);
            statement.setObject(10, year);
            
            // Execute the insert
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows > 0) {
                // Retrieve the generated ID
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        long articleId = generatedKeys.getLong(1);
                        LOGGER.info("Article inserted successfully with ID: " + articleId);
                        return articleId;
                    }
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to insert article: " + e.getMessage(), e);
        }
        
        return -1;
    }

    /**
     * Inserts a researcher into the researchers table
     * 
     * @param authorId Google Scholar author ID
     * @param name Researcher name
     * @param affiliation Researcher affiliation
     * @param email Researcher email
     * @param hIndex H-index
     * @param i10Index i10-index
     * @param totalCitations Total citations
     * @param interests Research interests
     * @param profileUrl Profile URL
     * @return Generated researcher ID, or -1 if insertion failed
     */
    public long insertResearcher(String authorId, String name, String affiliation, String email,
                               Integer hIndex, Integer i10Index, Integer totalCitations, 
                               String interests, String profileUrl) {
        
        String insertSQL = """
            INSERT INTO researchers (author_id, name, affiliation, email, h_index, i10_index, total_citations, interests, profile_url)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {
            
            // Set parameters
            statement.setString(1, authorId);
            statement.setString(2, name);
            statement.setString(3, affiliation);
            statement.setString(4, email);
            statement.setObject(5, hIndex);
            statement.setObject(6, i10Index);
            statement.setObject(7, totalCitations);
            statement.setString(8, interests);
            statement.setString(9, profileUrl);
            
            // Execute the insert
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows > 0) {
                // Retrieve the generated ID
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        long researcherId = generatedKeys.getLong(1);
                        LOGGER.info("Researcher inserted successfully with ID: " + researcherId);
                        return researcherId;
                    }
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to insert researcher: " + e.getMessage(), e);
        }
        
        return -1;
    }

    /**
     * Checks if a researcher with the given author ID already exists
     * 
     * @param authorId Google Scholar author ID
     * @return true if researcher exists, false otherwise
     */
    public boolean researcherExists(String authorId) {
        String selectSQL = "SELECT COUNT(*) FROM researchers WHERE author_id = ?";
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(selectSQL)) {
            
            statement.setString(1, authorId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to check if researcher exists: " + e.getMessage(), e);
        }
        
        return false;
    }

    /**
     * Gets researcher ID by author ID
     * 
     * @param authorId Google Scholar author ID
     * @return researcher ID or null if not found
     */
    public Long getResearcherIdByAuthorId(String authorId) {
        String selectSQL = "SELECT id FROM researchers WHERE author_id = ?";
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(selectSQL)) {
            
            statement.setString(1, authorId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getLong("id");
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to get researcher ID: " + e.getMessage(), e);
        }
        
        return null;
    }

    /**
     * Inserts an article into the articles table
     * 
     * @param title Article title
     * @param authors Article authors (comma-separated)
     * @param publicationDate Publication date
     * @param abstractText Article abstract
     * @param link Article link
     * @param keywords Article keywords (comma-separated)
     * @param citedBy Number of citations
     * @return Generated article ID, or -1 if insertion failed
     */
    public long insertArticle(String title, String authors, LocalDate publicationDate, 
                            String abstractText, String link, String keywords, int citedBy) {
        
        String insertSQL = """
            INSERT INTO articles (title, authors, publication_date, abstract, link, keywords, cited_by)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {
            
            // Set parameters
            statement.setString(1, title);
            statement.setString(2, authors);
            statement.setDate(3, publicationDate != null ? Date.valueOf(publicationDate) : null);
            statement.setString(4, abstractText);
            statement.setString(5, link);
            statement.setString(6, keywords);
            statement.setInt(7, citedBy);
            
            // Execute the insert
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows > 0) {
                // Retrieve the generated ID
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        long articleId = generatedKeys.getLong(1);
                        LOGGER.info("Article inserted successfully with ID: " + articleId);
                        return articleId;
                    }
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to insert article: " + e.getMessage(), e);
        }
        
        return -1;
    }
    
    /**
     * Bulk insert multiple articles
     * 
     * @param articles Array of article data [title, authors, date, abstract, link, keywords, citedBy]
     * @return Number of articles successfully inserted
     */
    public int bulkInsertArticles(Object[][] articles) {
        String insertSQL = """
            INSERT INTO articles (title, authors, publication_date, abstract, link, keywords, cited_by)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        
        int insertedCount = 0;
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(insertSQL)) {
            
            // Disable auto-commit for batch processing
            connection.setAutoCommit(false);
            
            for (Object[] article : articles) {
                try {
                    statement.setString(1, (String) article[0]); // title
                    statement.setString(2, (String) article[1]); // authors
                    statement.setDate(3, article[2] != null ? Date.valueOf((LocalDate) article[2]) : null); // date
                    statement.setString(4, (String) article[3]); // abstract
                    statement.setString(5, (String) article[4]); // link
                    statement.setString(6, (String) article[5]); // keywords
                    statement.setInt(7, (Integer) article[6]); // cited_by
                    
                    statement.addBatch();
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Failed to add article to batch: " + e.getMessage(), e);
                }
            }
            
            // Execute batch
            int[] results = statement.executeBatch();
            connection.commit();
            
            // Count successful inserts
            for (int result : results) {
                if (result > 0) {
                    insertedCount++;
                }
            }
            
            LOGGER.info("Bulk insert completed. Inserted " + insertedCount + " articles");
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Bulk insert failed: " + e.getMessage(), e);
        }
        
        return insertedCount;
    }
    
    /**
     * Checks if an article with the given title already exists
     * 
     * @param title Article title to check
     * @return true if article exists, false otherwise
     */
    public boolean articleExists(String title) {
        String selectSQL = "SELECT COUNT(*) FROM articles WHERE title = ?";
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(selectSQL)) {
            
            statement.setString(1, title);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to check if article exists: " + e.getMessage(), e);
        }
        
        return false;
    }
    
    /**
     * Updates citation count for an existing article
     * 
     * @param title Article title
     * @param newCitedBy New citation count
     * @return true if update was successful, false otherwise
     */
    public boolean updateCitationCount(String title, int newCitedBy) {
        String updateSQL = "UPDATE articles SET cited_by = ? WHERE title = ?";
        
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(updateSQL)) {
            
            statement.setInt(1, newCitedBy);
            statement.setString(2, title);
            
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows > 0) {
                LOGGER.info("Citation count updated for article: " + title);
                return true;
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to update citation count: " + e.getMessage(), e);
        }
        
        return false;
    }
    
    /**
     * Closes database resources safely
     * 
     * @param connection Database connection
     * @param statement Prepared statement
     * @param resultSet Result set
     */
    public void closeResources(Connection connection, PreparedStatement statement, ResultSet resultSet) {
        try {
            if (resultSet != null) resultSet.close();
            if (statement != null) statement.close();
            if (connection != null) connection.close();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error closing database resources: " + e.getMessage(), e);
        }
    }
    
    /**
     * Creates the articles and researchers tables if they don't exist
     * Useful for initial setup
     */
    public void createTablesIfNotExist() {
        String createResearchersTableSQL = """
            IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='researchers' AND xtype='U')
            CREATE TABLE researchers (
                id INT IDENTITY(1,1) PRIMARY KEY,
                author_id VARCHAR(50) NOT NULL UNIQUE,
                name VARCHAR(255) NOT NULL,
                affiliation VARCHAR(500),
                email VARCHAR(255),
                h_index INT,
                i10_index INT,
                total_citations INT,
                interests TEXT,
                profile_url TEXT
            )
            """;
        
        String createArticlesTableSQL = """
            IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='articles' AND xtype='U')
            CREATE TABLE articles (
                id INT IDENTITY(1,1) PRIMARY KEY,
                title VARCHAR(255) NOT NULL,
                authors TEXT,
                publication_date DATE,
                abstract TEXT,
                link TEXT,
                keywords TEXT,
                cited_by INT DEFAULT 0,
                researcher_id INT,
                citation_id VARCHAR(100),
                year INT,
                FOREIGN KEY (researcher_id) REFERENCES researchers(id)
            )
            """;
        
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            
            statement.execute(createResearchersTableSQL);
            LOGGER.info("Researchers table created or already exists");
            
            statement.execute(createArticlesTableSQL);
            LOGGER.info("Articles table created or already exists");
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to create tables: " + e.getMessage(), e);
        }
    }

    /**
     * Creates the articles table if it doesn't exist (deprecated - use createTablesIfNotExist)
     * Useful for initial setup
     */
    @Deprecated
    public void createArticlesTableIfNotExists() {
        createTablesIfNotExist();
    }
}