package com.fw.irongate.utils;

import static com.fw.irongate.constants.SystemConstants.JWT_CLAIM_KEY_USER_EMAIL;
import static com.fw.irongate.constants.SystemConstants.JWT_CLAIM_KEY_USER_FULL_NAME;
import static com.fw.irongate.constants.SystemConstants.JWT_CLAIM_KEY_USER_ID;
import static com.fw.irongate.constants.SystemConstants.JWT_CLAIM_KEY_USER_ROLE_ID;
import static com.fw.irongate.constants.SystemConstants.JWT_CLAIM_KEY_USER_ROLE_NAME;
import static com.fw.irongate.constants.SystemConstants.JWT_SUBJECT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import java.time.Instant;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class TestJwtUtil {

  @InjectMocks private JwtUtil jwtUtil;

  /* Test Data */
  private static final String TEST_SECRET = "my-super-secret-key-that-is-at-least-32-chars-long";
  private static final String TEST_ISSUER = "IrongateApp";
  private static final long TEST_EXPIRY_MS = 60000L; /* 1 minute */

  @BeforeEach
  void setUp() {
    /* Inject values normally read from application.properties */
    ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
    ReflectionTestUtils.setField(jwtUtil, "issuer", TEST_ISSUER);
    ReflectionTestUtils.setField(jwtUtil, "expiryInMillisecond", TEST_EXPIRY_MS);
  }

  @Test
  void shouldGenerateValidSignedJwtWithAllClaims() {
    /* --- Given --- */
    String userId = "1001";
    String email = "john.doe@example.com";
    String roleId = "55";
    String roleName = "ROLE_ADMIN";
    String fullName = "John Doe";
    /* --- When --- */
    String token = jwtUtil.generateJwt(userId, email, roleId, roleName, fullName);
    /* --- Then --- */
    assertNotNull(token);
    /* 1. Verify Signature & Integrity using the same Secret */
    Algorithm algorithm = Algorithm.HMAC256(TEST_SECRET);
    JWTVerifier verifier =
        JWT.require(algorithm)
            .withIssuer(TEST_ISSUER)
            /* We also verify the Subject if it's a constant in your class */
            /* .withSubject(JwtUtil.JWT_SUBJECT) */
            .build();
    DecodedJWT decodedJWT = verifier.verify(token);
    /* 2. Verify Claims Match Inputs */
    /* (Assuming your constants are public static, otherwise use the literal strings) */
    assertEquals(userId, decodedJWT.getClaim(JWT_CLAIM_KEY_USER_ID).asString());
    assertEquals(email, decodedJWT.getClaim(JWT_CLAIM_KEY_USER_EMAIL).asString());
    assertEquals(roleId, decodedJWT.getClaim(JWT_CLAIM_KEY_USER_ROLE_ID).asString());
    assertEquals(roleName, decodedJWT.getClaim(JWT_CLAIM_KEY_USER_ROLE_NAME).asString());
    assertEquals(fullName, decodedJWT.getClaim(JWT_CLAIM_KEY_USER_FULL_NAME).asString());
    /* 3. Verify Standard Claims (Subject, Issuer, Expiry) */
    assertEquals(JWT_SUBJECT, decodedJWT.getSubject());
    assertEquals(TEST_ISSUER, decodedJWT.getIssuer());
    /* 4. Verify Expiration Time */
    /* The token expiry should be roughly NOW + 60000ms */
    Date expiresAt = decodedJWT.getExpiresAt();
    assertNotNull(expiresAt);
    Instant nowPlusExpiry = Instant.now().plusMillis(TEST_EXPIRY_MS);
    /* Assert that the expiry is in the future, but not TOO far in the future (within 2 seconds tolerance) */
    assertTrue(expiresAt.toInstant().isAfter(Instant.now()));
    assertTrue(expiresAt.toInstant().isBefore(nowPlusExpiry.plusSeconds(2)));
  }

  @Test
  void shouldHandleNullArgumentsGracefully() {
    /* --- Given --- */
    /* Passing nulls for all user details */
    /* The java-jwt library typically omits the claim or sets it to null if the value is null */
    /* --- When --- */
    String token = jwtUtil.generateJwt(null, null, null, null, null);
    /* --- Then --- */
    assertNotNull(token);
    /* Verify the token is valid but has missing/null data */
    DecodedJWT decodedJWT = JWT.decode(token);
    /* Ensure the claims exist but are null/missing */
    assertTrue(decodedJWT.getClaim(JWT_CLAIM_KEY_USER_ID).isNull());
    assertTrue(decodedJWT.getClaim(JWT_CLAIM_KEY_USER_EMAIL).isNull());
    assertTrue(decodedJWT.getClaim(JWT_CLAIM_KEY_USER_FULL_NAME).isNull());
    /* Standard claims should still be there */
    assertEquals(TEST_ISSUER, decodedJWT.getIssuer());
  }

  @Test
  void shouldThrowException_WhenSecretIsNotConfigured() {
    /* --- Given --- */
    /* Simulate a misconfigured application (forgot to set property in .env) */
    ReflectionTestUtils.setField(jwtUtil, "secret", null);
    /* --- When & Then --- */
    /* The Algorithm.HMAC256(null) call throws IllegalArgumentException */
    assertThrows(
        IllegalArgumentException.class,
        () -> jwtUtil.generateJwt("1", "mail", "1", "role", "name"));
  }
}
