package com.fw.irongate.constants;

public interface SystemConstants {

  String OK = "OK";
  String COOKIE_NAME = "ff7ff393af8f";
  String JWT_SUBJECT = "user-data";
  String JWT_CLAIM_KEY_USER_ID = "userId";
  String JWT_CLAIM_KEY_USER_EMAIL = "email";
  String JWT_CLAIM_KEY_USER_ROLE_ID = "roleId";
  String JWT_CLAIM_KEY_USER_FULL_NAME = "fullName";
  String JWT_CLAIM_KEY_USER_ROLE_NAME = "roleName";
  String SYSTEM = "SYSTEM";
  String JSON_UNAUTHORIZED = "{\"error\":\"Unauthorized\"}";
  String JSON_INVALID_ROLE = "{\"error\":\"Invalid role\"}";
  String JSON_NO_PERMISSION = "{\"error\":\"No Permission\"}";
}
