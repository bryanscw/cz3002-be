package com.qwerty.cogbench.service;

import com.qwerty.cogbench.model.User;
import com.qwerty.cogbench.repository.UserRepository;
import java.util.Collections;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Slf4j
public class DefaultAuthenticationProvider implements AuthenticationProvider {

  private final UserRepository userRepository;

  public DefaultAuthenticationProvider(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public Authentication authenticate(final Authentication authentication)
      throws AuthenticationException {

    if (authentication.getName() == null || authentication.getCredentials() == null) {
      return null;
    }

    if (authentication.getName().isEmpty() || authentication.getCredentials().toString()
        .isEmpty()) {
      return null;
    }

    final Optional<User> appUser = this.userRepository.findById(authentication.getName());

    if (appUser.isPresent()) {
      final User user = appUser.get();
      final String providedUserEmail = authentication.getName();
      final Object providedUserPassword = authentication.getCredentials();

      if (providedUserEmail.equalsIgnoreCase(user.getEmail())
          && providedUserPassword.equals(user.getPass())) {
        return new UsernamePasswordAuthenticationToken(
            user.getEmail(),
            user.getPass(),
            Collections.singleton(new SimpleGrantedAuthority(user.getRole())));
      }
    }

    log.error("Invalid user name or password");
    throw new UsernameNotFoundException("Invalid username or password.");
  }

  @Override
  public boolean supports(final Class<?> authentication) {
    return authentication.equals(UsernamePasswordAuthenticationToken.class);
  }

}
