package com.example.resumescreener.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration for serving static files.
 * Allows the frontend HTML/CSS/JS files to be served at root.
 * APIs are served at /api/* paths.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(@org.springframework.lang.NonNull ResourceHandlerRegistry registry) {
        // Serve static files (HTML, CSS, JS) at root
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }

    @Override
    public void addViewControllers(@org.springframework.lang.NonNull ViewControllerRegistry registry) {
        // Redirect root to index.html
        registry.addViewController("/").setViewName("forward:/index.html");
    }
}
