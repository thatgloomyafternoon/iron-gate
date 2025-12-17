package com.fw.irongate.usecases.login;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.fw.irongate.models.dto.SecurityUserDTO;
import com.fw.irongate.models.entities.Sysconfig;
import com.fw.irongate.models.entities.SysconfigType;
import com.fw.irongate.models.entities.User;
import com.fw.irongate.repositories.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class UserDetailsUseCaseTest {

  @Mock private UserRepository userRepository;

  @InjectMocks private UserDetailsUseCase userDetailsUseCase;

  @Test
  void shouldLoadUserSuccessfully_WhenUserExists() {
    /* --- Given --- */
    String email = "alice@example.com";
    /* 1. Create Role Entity */
    SysconfigType role = new SysconfigType();
    role.setCreatedBy("system");
    role.setUpdatedBy("system");
    role.setName("ROLE");
    role.setDescription("asd");
    Sysconfig manager = new Sysconfig();
    manager.setCreatedBy("system");
    manager.setUpdatedBy("system");
    manager.setKey("MANAGER");
    manager.setValue("ROLE_MANAGER");
    /* 2. Create User Entity */
    User userEntity = new User();
    userEntity.setEmail(email);
    userEntity.setPasswordHash("hashed_secret");
    userEntity.setFullName("Alice Wonderland");
    userEntity.setRole(manager);
    /* 3. Mock the Repository */
    given(userRepository.findOneActiveByEmail(email)).willReturn(Optional.of(userEntity));
    /* --- When --- */
    UserDetails result = userDetailsUseCase.loadUserByUsername(email);
    /* --- Then --- */
    assertNotNull(result);
    /* 1. Check if the type is correct */
    assertInstanceOf(SecurityUserDTO.class, result);
    SecurityUserDTO securityUser = (SecurityUserDTO) result;
    /* 2. Verify all mapped fields match the Entity */
    assertEquals(userEntity.getId(), securityUser.id());
    assertEquals(userEntity.getEmail(), securityUser.email());
    assertEquals(userEntity.getPasswordHash(), securityUser.password());
    assertEquals(userEntity.getRole().getId(), securityUser.roleId());
    assertEquals(userEntity.getRole().getValue(), securityUser.roleName());
    assertEquals(userEntity.getFullName(), securityUser.fullName());
    /* 3. Verify Authorities (Roles) */
    assertEquals(1, securityUser.getAuthorities().size());
    assertEquals("ROLE_MANAGER", securityUser.getAuthorities().iterator().next().getAuthority());
  }

  @Test
  void shouldThrowException_WhenUserNotFound() {
    /* --- Given --- */
    String email = "ghost@example.com";
    /* Mock repository returning Empty */
    given(userRepository.findOneActiveByEmail(email)).willReturn(Optional.empty());
    /* --- When & Then --- */
    assertThrows(
        UsernameNotFoundException.class, () -> userDetailsUseCase.loadUserByUsername(email));
    /* Verify repository was actually called */
    verify(userRepository).findOneActiveByEmail(email);
  }
}
