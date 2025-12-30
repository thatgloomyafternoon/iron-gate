package com.fw.irongate.utils;

import static com.fw.irongate.constants.SystemConstants.JWT_CLAIM_KEY_USER_EMAIL;
import static com.fw.irongate.constants.SystemConstants.JWT_CLAIM_KEY_USER_FULL_NAME;
import static com.fw.irongate.constants.SystemConstants.JWT_CLAIM_KEY_USER_ID;
import static com.fw.irongate.constants.SystemConstants.JWT_CLAIM_KEY_USER_ROLE_ID;
import static com.fw.irongate.constants.SystemConstants.JWT_CLAIM_KEY_USER_ROLE_NAME;
import static com.fw.irongate.constants.SystemConstants.JWT_SUBJECT;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.fw.irongate.models.dto.JwtClaimDTO;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.expiry_in_millisecond}")
  private long expiryInMillisecond;

  @Value("${jwt.issuer}")
  private String issuer;

  private final JWTVerifier jwtVerifier;

  public JwtUtil(JWTVerifier jwtVerifier) {
    this.jwtVerifier = jwtVerifier;
  }

  public String generateJwt(
      String userId, String email, String roleId, String roleName, String fullName) {
    return JWT.create()
        .withSubject(JWT_SUBJECT)
        .withClaim(JWT_CLAIM_KEY_USER_ID, userId)
        .withClaim(JWT_CLAIM_KEY_USER_EMAIL, email)
        .withClaim(JWT_CLAIM_KEY_USER_ROLE_ID, roleId)
        .withClaim(JWT_CLAIM_KEY_USER_ROLE_NAME, roleName)
        .withClaim(JWT_CLAIM_KEY_USER_FULL_NAME, fullName)
        .withIssuer(issuer)
        .withExpiresAt(new Date(System.currentTimeMillis() + expiryInMillisecond))
        .sign(Algorithm.HMAC256(secret));
  }

  public JwtClaimDTO validateJwt(String jwt) {
    try {
      var decodedJWT = jwtVerifier.verify(jwt);
      return new JwtClaimDTO(
          UUID.fromString(decodedJWT.getClaim(JWT_CLAIM_KEY_USER_ID).asString()),
          decodedJWT.getClaim(JWT_CLAIM_KEY_USER_EMAIL).asString(),
          UUID.fromString(decodedJWT.getClaim(JWT_CLAIM_KEY_USER_ROLE_ID).asString()),
          decodedJWT.getClaim(JWT_CLAIM_KEY_USER_ROLE_NAME).asString(),
          decodedJWT.getClaim(JWT_CLAIM_KEY_USER_FULL_NAME).asString());
    } catch (Exception e) {
      return null;
    }
  }

  public Instant extractExpiration(String jwt) {
    return JWT.decode(jwt).getExpiresAtAsInstant();
  }
}
