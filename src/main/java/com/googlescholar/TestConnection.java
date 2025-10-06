package com.googlescholar;

import java.sql.*;

public class TestConnection {
    public static void main(String[] args) {
        String[] urls = {
            "jdbc:sqlserver://localhost\\SQLEXPRESS2025;databaseName=googlescholar;trustServerCertificate=true;encrypt=false;integratedSecurity=true",
            "jdbc:sqlserver://localhost\\SQLEXPRESS2025;databaseName=googlescholar;trustServerCertificate=true;encrypt=false;integratedSecurity=false;user=root;password=root",
            "jdbc:sqlserver://localhost:53580;databaseName=googlescholar;trustServerCertificate=true;encrypt=false",
            "jdbc:sqlserver://localhost:1433;databaseName=googlescholar;trustServerCertificate=true;encrypt=false"
        };
        
        String username = "root";
        String password = "root";
        
        for (int i = 0; i < urls.length; i++) {
            String url = urls[i];
            System.out.println("\n🔗 Probando conexión " + (i+1) + ": " + url);
            try {
                Connection connection;
                if (i == 0) {
                    // Para autenticación integrada, no usar usuario/contraseña
                    connection = DriverManager.getConnection(url);
                } else {
                    connection = DriverManager.getConnection(url, username, password);
                }
                System.out.println("✅ CONEXIÓN EXITOSA!");
                connection.close();
                break; // Si encuentra una que funciona, la usa
            } catch (SQLException e) {
                System.out.println("❌ Error: " + e.getMessage());
            }
        }
    }
}