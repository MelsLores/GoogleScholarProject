package com.googlescholar.example;

import com.googlescholar.service.MultiResearcherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Example class demonstrating how to process 2 researchers with 3 articles each
 * Total: 6 articles from 2 researchers using Google Scholar Author Articles API
 * 
 * @author Melany Rivera
 * @since October 2, 2025
 */
@Component
public class MultiResearcherExample {
    
    @Autowired
    private MultiResearcherService multiResearcherService;
    
    /**
     * Example of processing 2 researchers with their articles
     * This method shows how to structure the data for the API
     */
    public void processExampleResearchers() {
        List<MultiResearcherService.ResearcherData> researchers = new ArrayList<>();
        
        // Researcher 1: Example from your SerpApi documentation
        MultiResearcherService.ResearcherData researcher1 = new MultiResearcherService.ResearcherData();
        researcher1.setAuthorId("EicYvbwAAAAJ");
        researcher1.setName("RC Kessler");
        researcher1.setAffiliation("Harvard Medical School");
        researcher1.setHIndex(120);
        researcher1.setI10Index(250);
        researcher1.setTotalCitations(50000);
        researcher1.setInterests("Psychiatric epidemiology, Mental health services");
        researcher1.setProfileUrl("https://scholar.google.com/citations?user=EicYvbwAAAAJ");
        
        // Example JSON from Google Scholar Author Articles API for researcher 1
        String researcher1ArticlesJson = """
        {
          "articles": [
            {
              "title": "Lifetime prevalence and age-of-onset distributions of DSM-IV disorders in the National Comorbidity Survey Replication",
              "link": "https://scholar.google.com/citations?view_op=view_citation&hl=en&user=EicYvbwAAAAJ&citation_for_view=EicYvbwAAAAJ:UeHWp8X0CEIC",
              "citation_id": "EicYvbwAAAAJ:UeHWp8X0CEIC",
              "authors": "RC Kessler, P Berglund, O Demler, R Jin, KR Merikangas, EE Walters",
              "publication": "Archives of general psychiatry 62 (6), 593-602, 2005",
              "cited_by": {
                "value": 29693,
                "link": "https://scholar.google.com/scholar?oi=bibs&hl=en&cites=2173726401600747709",
                "cites_id": "2173726401600747709"
              },
              "year": "2005"
            },
            {
              "title": "Lifetime and 12-month prevalence of DSM-III-R psychiatric disorders in the United States",
              "link": "https://scholar.google.com/citations?view_op=view_citation&hl=en&user=EicYvbwAAAAJ&citation_for_view=EicYvbwAAAAJ:u5HHmVD_uO8C",
              "citation_id": "EicYvbwAAAAJ:u5HHmVD_uO8C",
              "authors": "RC Kessler, KA McGonagle, S Zhao, CB Nelson, M Hughes, S Eshleman",
              "publication": "Archives of general psychiatry 51 (1), 8-19, 1994",
              "cited_by": {
                "value": 18077,
                "link": "https://scholar.google.com/scholar?oi=bibs&hl=en&cites=9166229658659884654",
                "cites_id": "9166229658659884654"
              },
              "year": "1994"
            },
            {
              "title": "Prevalence, severity, and comorbidity of 12-month DSM-IV disorders in the National Comorbidity Survey Replication",
              "link": "https://scholar.google.com/citations?view_op=view_citation&hl=en&user=EicYvbwAAAAJ&citation_for_view=EicYvbwAAAAJ:qjMakFHDy7sC",
              "citation_id": "EicYvbwAAAAJ:qjMakFHDy7sC",
              "authors": "RC Kessler, WT Chiu, O Demler, EE Walters",
              "publication": "Archives of general psychiatry 62 (6), 617-627, 2005",
              "cited_by": {
                "value": 14533,
                "link": "https://scholar.google.com/scholar?oi=bibs&hl=en&cites=9617930262438990334",
                "cites_id": "9617930262438990334"
              },
              "year": "2005"
            }
          ]
        }
        """;
        researcher1.setArticlesJson(researcher1ArticlesJson);
        researchers.add(researcher1);
        
        // Researcher 2: Second example researcher
        MultiResearcherService.ResearcherData researcher2 = new MultiResearcherService.ResearcherData();
        researcher2.setAuthorId("ABCD1234567");
        researcher2.setName("Jane Smith");
        researcher2.setAffiliation("Stanford University");
        researcher2.setHIndex(85);
        researcher2.setI10Index(150);
        researcher2.setTotalCitations(25000);
        researcher2.setInterests("Machine Learning, Artificial Intelligence, Computer Vision");
        researcher2.setProfileUrl("https://scholar.google.com/citations?user=ABCD1234567");
        
        // Example JSON for researcher 2
        String researcher2ArticlesJson = """
        {
          "articles": [
            {
              "title": "Deep Learning Approaches for Medical Image Analysis",
              "link": "https://scholar.google.com/citations?view_op=view_citation&hl=en&user=ABCD1234567&citation_for_view=ABCD1234567:ABC123",
              "citation_id": "ABCD1234567:ABC123",
              "authors": "Jane Smith, John Doe, Mary Johnson",
              "publication": "Nature Machine Intelligence 5 (2), 123-145, 2023",
              "cited_by": {
                "value": 1250,
                "link": "https://scholar.google.com/scholar?oi=bibs&hl=en&cites=1234567890",
                "cites_id": "1234567890"
              },
              "year": "2023"
            },
            {
              "title": "Automated Diagnosis Using Convolutional Neural Networks",
              "link": "https://scholar.google.com/citations?view_op=view_citation&hl=en&user=ABCD1234567&citation_for_view=ABCD1234567:DEF456",
              "citation_id": "ABCD1234567:DEF456",
              "authors": "Jane Smith, Alice Brown, Bob Wilson",
              "publication": "IEEE Transactions on Medical Imaging 41 (8), 1856-1867, 2022",
              "cited_by": {
                "value": 890,
                "link": "https://scholar.google.com/scholar?oi=bibs&hl=en&cites=2345678901",
                "cites_id": "2345678901"
              },
              "year": "2022"
            },
            {
              "title": "Federated Learning for Healthcare Applications",
              "link": "https://scholar.google.com/citations?view_op=view_citation&hl=en&user=ABCD1234567&citation_for_view=ABCD1234567:GHI789",
              "citation_id": "ABCD1234567:GHI789",
              "authors": "Jane Smith, David Lee, Sarah Taylor",
              "publication": "Journal of Medical Internet Research 24 (3), e12345, 2022",
              "cited_by": {
                "value": 567,
                "link": "https://scholar.google.com/scholar?oi=bibs&hl=en&cites=3456789012",
                "cites_id": "3456789012"
              },
              "year": "2022"
            }
          ]
        }
        """;
        researcher2.setArticlesJson(researcher2ArticlesJson);
        researchers.add(researcher2);
        
        // Process the researchers
        System.out.println("=== Processing Multiple Researchers Example ===");
        System.out.println("Total researchers: " + researchers.size());
        System.out.println("Expected total articles: 6 (3 per researcher)");
        
        MultiResearcherService.MultiResearcherResult result = 
            multiResearcherService.processMultipleResearchers(researchers);
        
        // Print results
        System.out.println("\n=== Processing Results ===");
        System.out.println("Successful researchers: " + result.getSuccessfulResearchers());
        System.out.println("Failed researchers: " + result.getFailedResearchers());
        System.out.println("Total articles processed: " + result.getTotalArticlesProcessed());
        System.out.println("Total articles saved: " + result.getTotalArticlesSaved());
        System.out.println("Total articles skipped: " + result.getTotalArticlesSkipped());
        System.out.println("Total articles updated: " + result.getTotalArticlesUpdated());
        
        if (result.hasError()) {
            System.out.println("Error: " + result.getError());
        }
        
        // Print details for each researcher
        for (MultiResearcherService.ResearcherProcessingResult resResult : result.getResearcherResults()) {
            System.out.println("\n--- Researcher: " + resResult.getName() + " (" + resResult.getAuthorId() + ") ---");
            System.out.println("Researcher saved: " + resResult.isResearcherSaved());
            System.out.println("Researcher updated: " + resResult.isResearcherUpdated());
            System.out.println("Articles in response: " + resResult.getTotalArticlesInResponse());
            System.out.println("Articles processed: " + resResult.getArticlesProcessed());
            System.out.println("Articles saved: " + resResult.getArticlesSaved());
            System.out.println("Articles skipped: " + resResult.getArticlesSkipped());
            
            if (resResult.hasError()) {
                System.out.println("Error: " + resResult.getError());
            }
        }
    }
    
    /**
     * Returns example curl command for testing the API
     */
    public String getExampleCurlCommand() {
        return """
        curl -X POST http://localhost:8080/api/v1/scholar/process-multiple-researchers \\
          -H "Content-Type: application/json" \\
          -d '[
            {
              "authorId": "EicYvbwAAAAJ",
              "name": "RC Kessler",
              "affiliation": "Harvard Medical School",
              "hIndex": 120,
              "i10Index": 250,
              "totalCitations": 50000,
              "interests": "Psychiatric epidemiology, Mental health services",
              "profileUrl": "https://scholar.google.com/citations?user=EicYvbwAAAAJ",
              "articlesJson": "{\\"articles\\": [...]} // JSON from Author Articles API"
            },
            {
              "authorId": "ABCD1234567",
              "name": "Jane Smith",
              "affiliation": "Stanford University",
              "hIndex": 85,
              "i10Index": 150,
              "totalCitations": 25000,
              "interests": "Machine Learning, AI, Computer Vision",
              "profileUrl": "https://scholar.google.com/citations?user=ABCD1234567",
              "articlesJson": "{\\"articles\\": [...]} // JSON from Author Articles API"
            }
          ]'
        """;
    }
}