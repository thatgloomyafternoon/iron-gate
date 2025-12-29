package com.fw.irongate.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS (Cross-Origin Resource Sharing) is only required when the Browser sees two different "Origins".<br>
 * An origin is defined by Protocol + Domain + Port.<br>
 * 1. Scenario A: Your Current Setup (Development)<br>
 *    - Frontend: http://localhost:5173 (Vite)<br>
 *    - Backend: http://localhost:8080 (Spring Boot)<br>
 *    - Verdict: These are Different Origins (different ports). The browser blocks the connection for security unless you allow it via CORS.<br>
 * 2. Scenario B: The "Resources" Deployment (Production)<br>
 * If you run npm run build and copy the output files to Spring Boot's static folder:<br>
 *    - Frontend: http://localhost:8080 (Served by Tomcat)<br>
 *    - Backend: http://localhost:8080/api/... (Served by Tomcat)<br>
 *    - Verdict: Same Origin. The browser allows this automatically. No configuration needed.
 */
@Configuration
public class CorsConfig {

  @Bean
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      @SuppressWarnings("NullableProblems")
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        registry
            .addMapping("/**")
            .allowedOrigins("http://localhost:5173", "https://gloomyafternoon.xyz")
            .allowedMethods(
                HttpMethod.GET.name(),
                HttpMethod.POST.name(),
                HttpMethod.PATCH.name(),
                HttpMethod.PUT.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.OPTIONS.name())
            .allowedHeaders("*")
            .allowCredentials(true);
      }
    };
  }
}
