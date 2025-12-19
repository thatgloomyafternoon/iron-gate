package com.fw.irongate.usecases.login;

import static com.fw.irongate.constants.MessageConstants.USER_EMAIL_NOT_FOUND;

import com.fw.irongate.models.dto.SecurityUserDTO;
import com.fw.irongate.models.entities.User;
import com.fw.irongate.repositories.UserRepository;
import com.fw.irongate.usecases.UseCase;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@UseCase
public class UserDetailsUseCase implements UserDetailsService {

  private final UserRepository userRepository;

  public UserDetailsUseCase(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  /**
   * This method will be called from inside the execution scope of
   * AuthenticationManager.authenticate(UsernamePasswordAuthenticationToken)
   */
  @Override
  public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
    User user =
        userRepository
            .findOneActiveByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException(USER_EMAIL_NOT_FOUND));
    List<GrantedAuthority> authorities =
        List.of(new SimpleGrantedAuthority(user.getRole().getValue()));
    return new SecurityUserDTO(
        user.getId(),
        user.getEmail(),
        user.getPasswordHash(),
        user.getRole().getId(),
        user.getRole().getValue(),
        user.getFullName(),
        authorities);
  }
}
