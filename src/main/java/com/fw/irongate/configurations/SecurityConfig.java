package com.fw.irongate.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * When your application starts, Spring Boot's AuthenticationConfiguration (the class you used to
 * get the manager) scans your application context:<br>
 * (1) Scan: It looks for a Bean that implements UserDetailsService.<br>
 * (2) Found: It finds your UserDetailsUseCase bean.<br>
 * (3) Wire: It automatically creates an instance of DaoAuthenticationProvider and injects your UserDetailsUseCase into it.<br>
 * (4) Register: It registers this provider inside the global AuthenticationManager.
 */
@Configuration
public class SecurityConfig {

  private final JwtFilter jwtFilter;

  public SecurityConfig(JwtFilter jwtFilter) {
    this.jwtFilter = jwtFilter;
  }

  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
    httpSecurity
        .csrf(AbstractHttpConfigurer::disable)
        .cors(Customizer.withDefaults())
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/")
                    .permitAll()
                    .requestMatchers(
                        "/index.html",
                        "/assets/**",
                        "/static/**",
                        "/*.js",
                        "/*.css",
                        "/*.ico",
                        "/*.png",
                        "/*.svg")
                    .permitAll()
                    .requestMatchers("/api/auth/login")
                    .permitAll()
                    .requestMatchers("/api/**")
                    .authenticated()
                    .anyRequest()
                    .permitAll());
    httpSecurity.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    return httpSecurity.build();
  }
}
