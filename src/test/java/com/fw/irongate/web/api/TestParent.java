package com.fw.irongate.web.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fw.irongate.models.entities.Sysconfig;
import com.fw.irongate.models.entities.SysconfigType;
import com.fw.irongate.models.entities.User;
import com.fw.irongate.repositories.SysconfigRepository;
import com.fw.irongate.repositories.SysconfigTypeRepository;
import com.fw.irongate.repositories.UserRepository;
import com.fw.irongate.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SuppressWarnings("SameParameterValue")
public abstract class TestParent {

  @Autowired protected SysconfigTypeRepository sysconfigTypeRepository;
  @Autowired protected SysconfigRepository sysconfigRepository;
  @Autowired protected UserRepository userRepository;
  @Autowired protected JwtUtil jwtUtil;

  @Autowired protected MockMvc mockMvc;
  @Autowired protected BCryptPasswordEncoder bCryptPasswordEncoder;
  @Autowired protected ObjectMapper objectMapper;

  protected void deleteAll() {
    userRepository.deleteAll();
    sysconfigRepository.deleteAll();
    sysconfigTypeRepository.deleteAll();
  }

  protected SysconfigType createSysconfigType(
      SysconfigTypeRepository repository, String name, String description) {
    SysconfigType st = new SysconfigType();
    st.setCreatedBy("system");
    st.setUpdatedBy("system");
    st.setName(name);
    st.setDescription(description);
    return repository.save(st);
  }

  protected Sysconfig createSysconfig(
      SysconfigRepository repository, SysconfigType sysconfigType, String key, String value) {
    Sysconfig sysconfig = new Sysconfig();
    sysconfig.setCreatedBy("system");
    sysconfig.setUpdatedBy("system");
    sysconfig.setSysconfigType(sysconfigType);
    sysconfig.setKey(key);
    sysconfig.setValue(value);
    return repository.save(sysconfig);
  }

  protected User createUser(
      UserRepository repository,
      Sysconfig role,
      String email,
      String passwordHash,
      String fullName) {
    User user = new User();
    user.setCreatedBy("system");
    user.setUpdatedBy("system");
    user.setRole(role);
    user.setEmail(email);
    user.setPasswordHash(passwordHash);
    user.setFullName(fullName);
    return repository.save(user);
  }
}
