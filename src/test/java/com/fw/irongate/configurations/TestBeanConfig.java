package com.fw.irongate.configurations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.auth0.jwt.JWTVerifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

class TestBeanConfig {

  private final ApplicationContextRunner contextRunner =
      new ApplicationContextRunner().withUserConfiguration(BeanConfig.class);

  @Test
  void shouldRegisterJWTVerifier() {
    contextRunner.run(
        context -> {
          assertThat(context).hasSingleBean(JWTVerifier.class);
          assertNotNull(context.getBean(JWTVerifier.class));
        });
  }

  @Test
  void shouldRegisterPasswordEncoder() {
    contextRunner.run(
        context -> {
          assertThat(context).hasSingleBean(PasswordEncoder.class);
          assertNotNull(context.getBean(PasswordEncoder.class));
        });
  }

  @Test
  void shouldRegisterObjectMapper() {
    contextRunner.run(
        context -> {
          assertThat(context).hasSingleBean(ObjectMapper.class);
          assertNotNull(context.getBean(ObjectMapper.class));
        });
  }
}
