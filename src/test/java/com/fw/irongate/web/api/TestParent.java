package com.fw.irongate.web.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fw.irongate.models.entities.Permission;
import com.fw.irongate.models.entities.Product;
import com.fw.irongate.models.entities.Sysconfig;
import com.fw.irongate.models.entities.SysconfigType;
import com.fw.irongate.models.entities.User;
import com.fw.irongate.repositories.PermissionRepository;
import com.fw.irongate.repositories.ProductRepository;
import com.fw.irongate.repositories.RevokedTokenRepository;
import com.fw.irongate.repositories.SysconfigRepository;
import com.fw.irongate.repositories.SysconfigTypeRepository;
import com.fw.irongate.repositories.UserRepository;
import com.fw.irongate.utils.JwtUtil;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@SuppressWarnings({"SameParameterValue", "UnusedReturnValue"})
public abstract class TestParent {

  @Autowired protected SysconfigTypeRepository sysconfigTypeRepository;
  @Autowired protected SysconfigRepository sysconfigRepository;
  @Autowired protected UserRepository userRepository;
  @Autowired protected PermissionRepository permissionRepository;
  @Autowired protected RevokedTokenRepository revokedTokenRepository;
  @Autowired protected ProductRepository productRepository;
  @Autowired protected JwtUtil jwtUtil;

  @Autowired protected MockMvc mockMvc;
  @Autowired protected BCryptPasswordEncoder bCryptPasswordEncoder;
  @Autowired protected ObjectMapper objectMapper;

  protected void deleteAll() {
    productRepository.deleteAll();
    revokedTokenRepository.deleteAll();
    permissionRepository.deleteAll();
    userRepository.deleteAll();
    sysconfigRepository.deleteAll();
    sysconfigTypeRepository.deleteAll();
  }

  protected SysconfigType createSysconfigType(String name, String description) {
    SysconfigType st = new SysconfigType();
    st.setCreatedBy("system");
    st.setUpdatedBy("system");
    st.setName(name);
    st.setDescription(description);
    return sysconfigTypeRepository.save(st);
  }

  protected Sysconfig createSysconfig(SysconfigType sysconfigType, String key, String value) {
    Sysconfig sysconfig = new Sysconfig();
    sysconfig.setCreatedBy("system");
    sysconfig.setUpdatedBy("system");
    sysconfig.setSysconfigType(sysconfigType);
    sysconfig.setKey(key);
    sysconfig.setValue(value);
    return sysconfigRepository.save(sysconfig);
  }

  protected User createUser(Sysconfig role, String email, String passwordHash, String fullName) {
    User user = new User();
    user.setCreatedBy("system");
    user.setUpdatedBy("system");
    user.setRole(role);
    user.setEmail(email);
    user.setPasswordHash(passwordHash);
    user.setFullName(fullName);
    return userRepository.save(user);
  }

  protected Permission createPermission(Sysconfig role, Sysconfig resourcePath) {
    Permission permission = new Permission();
    permission.setCreatedBy("system");
    permission.setUpdatedBy("system");
    permission.setResourcePath(resourcePath);
    permission.setRole(role);
    return permissionRepository.save(permission);
  }

  protected Product createProduct(String name, String sku, String description, BigDecimal price) {
    Product product = new Product();
    product.setCreatedBy("system");
    product.setUpdatedBy("system");
    product.setName(name);
    product.setSku(sku);
    product.setDescription(description);
    product.setPrice(price);
    return productRepository.save(product);
  }
}
