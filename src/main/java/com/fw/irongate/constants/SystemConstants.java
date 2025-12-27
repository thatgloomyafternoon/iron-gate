package com.fw.irongate.constants;

public interface SystemConstants {

  String OK = "OK";
  String ERROR = "ERROR";
  String COOKIE_NAME = "ff7ff393af8f";
  String JWT_SUBJECT = "user-data";
  String JWT_CLAIM_KEY_USER_ID = "userId";
  String JWT_CLAIM_KEY_USER_EMAIL = "email";
  String JWT_CLAIM_KEY_USER_ROLE_ID = "roleId";
  String JWT_CLAIM_KEY_USER_FULL_NAME = "fullName";
  String JWT_CLAIM_KEY_USER_ROLE_NAME = "roleName";
  String SYSTEM = "SYSTEM";
  String SIMULATION_RUN_FLAG = "SIMULATION_RUN_FLAG";
  String JSON_UNAUTHORIZED = "{\"error\":\"Unauthorized\"}";
  String JSON_INVALID_ROLE = "{\"error\":\"Invalid role\"}";
  String JSON_NO_PERMISSION = "{\"error\":\"No Permission\"}";

  String EVENT_ORDER_CREATED = "ORDER_CREATED";
  String EVENT_ORDER_UPDATED = "ORDER_UPDATED";
  String EVENT_SHIPMENT_CREATED = "SHIPMENT_CREATED";
  String EVENT_SHIPMENT_UPDATED = "SHIPMENT_UPDATED";
}
