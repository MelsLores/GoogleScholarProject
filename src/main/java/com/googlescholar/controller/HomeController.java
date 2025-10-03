package com.googlescholar.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

import io.swagger.v3.oas.annotations.Hidden;

/**
 * Home Controller for Google Scholar API
 * Provides navigation and redirection to API documentation
 * 
 * @author Melany Rivera
 * @date October 2, 2025
 */
@Controller
public class HomeController {

    /**
     * Redirect root path to Swagger UI
     * 
     * @return redirect to Swagger UI documentation
     */
    @Hidden // Hide from Swagger documentation
    @GetMapping("/")
    public RedirectView home() {
        return new RedirectView("/swagger-ui.html");
    }

    /**
     * Redirect /api to Swagger UI
     * 
     * @return redirect to Swagger UI documentation
     */
    @Hidden // Hide from Swagger documentation
    @GetMapping("/api")
    public RedirectView api() {
        return new RedirectView("/swagger-ui.html");
    }

    /**
     * Redirect /docs to Swagger UI
     * 
     * @return redirect to Swagger UI documentation
     */
    @Hidden // Hide from Swagger documentation
    @GetMapping("/docs")
    public RedirectView docs() {
        return new RedirectView("/swagger-ui.html");
    }
}