package com.fw.irongate.models.dto;

import java.util.Collection;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public record SecurityUserDTO(
    UUID id,
    String email,
    String password,
    UUID roleId,
    String roleName,
    String fullName,
    Collection<? extends GrantedAuthority> authorities)
    implements UserDetails {

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }
}
