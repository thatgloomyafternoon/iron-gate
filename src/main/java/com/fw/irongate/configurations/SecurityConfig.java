package com.fw.irongate.configurations;

import com.fw.irongate.usecases.login.UserDetailsUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

  private final UserDetailsUseCase userDetailsUseCase;
  private final JwtFilter jwtFilter;

  public SecurityConfig(UserDetailsUseCase userDetailsUseCase, JwtFilter jwtFilter) {
    this.userDetailsUseCase = userDetailsUseCase;
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
        .cors(AbstractHttpConfigurer::disable)
        .sessionManagement(
            session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .userDetailsService(userDetailsUseCase)
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
