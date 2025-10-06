package com.googlescholar;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SimpleConnectionTest {
    public static void main(String[] args) {
        // Test Named Pipes connection (matches our application.properties)
        String connectionString = "jdbc:sqlserver://localhost;instanceName=SQLEXPRESS2025;databaseName=googlescholar;trustServerCertificate=true;encrypt=false;integratedSecurity=true;namedPipe=true";
        
        System.out.println("Testing Named Pipes connection with: " + connectionString);
        
        try {
            // Load driver
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            
            // Test connection
            Connection connection = DriverManager.getConnection(connectionString);
            
            if (connection != null && !connection.isClosed()) {
                System.out.println("✓ SUCCESS: Connected to SQL Server successfully!");
                System.out.println("Database: " + connection.getCatalog());
                System.out.println("Schema: " + connection.getSchema());
                
                // Test a simple query
                var statement = connection.createStatement();
                var resultSet = statement.executeQuery("SELECT @@VERSION");
                if (resultSet.next()) {
                    System.out.println("SQL Server Version: " + resultSet.getString(1).substring(0, 50) + "...");
                }
                
                connection.close();
                System.out.println("✓ Connection closed successfully");
            } else {
                System.out.println("✗ FAILED: Connection is null or closed");
            }
            
        } catch (ClassNotFoundException e) {
            System.out.println("✗ FAILED: SQL Server JDBC Driver not found");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("✗ FAILED: " + e.getMessage());
            System.out.println("Error Code: " + e.getErrorCode());
            System.out.println("SQL State: " + e.getSQLState());
        }
    }
}