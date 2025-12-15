package com.fw.irongate.configurations;

import com.fw.irongate.usecases.login.UserDetailsUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
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
  private final UserDetailsUseCase userDetailsUseCase;
  private final PasswordEncoder passwordEncoder;

  public SecurityConfig(
      JwtFilter jwtFilter, UserDetailsUseCase userDetailsUseCase, PasswordEncoder passwordEncoder) {
    this.jwtFilter = jwtFilter;
    this.userDetailsUseCase = userDetailsUseCase;
    this.passwordEncoder = passwordEncoder;
  }

  @Bean
  public AuthenticationManager authenticationManager(
      AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

  @Bean
  public DaoAuthenticationProvider daoAuthenticationProvider() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsUseCase);
    provider.setPasswordEncoder(passwordEncoder);
    return provider;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
    httpSecurity
        .csrf(AbstractHttpConfigurer::disable)
        .cors(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers("/")
                    .permitAll()
                    .requestMatchers("/index.html")
                    .permitAll()
                    .requestMatchers("/assets/")
                    .permitAll()
                    .requestMatchers("/static/")
                    .permitAll()
                    .requestMatchers("/api/auth/login")
                    .permitAll()
                    .anyRequest()
                    .authenticated());
    httpSecurity.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    return httpSecurity.build();
  }
}
