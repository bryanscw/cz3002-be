package com.qwerty.cogbench.service;

import com.qwerty.cogbench.model.User;
import com.qwerty.cogbench.repository.UserRepository;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class DefaultUserDetailsService implements UserDetailsService {

  // TODO: Autowired annotation can be removed? (please check)
  @Autowired
  private UserRepository userRepository;

  public DefaultUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    final String errorMessage = String.format("User [%s] not found", username);
    final User userEntity = userRepository.findById(username)
        .orElseThrow(
            () -> new UsernameNotFoundException(errorMessage)
        );

    return new org.springframework.security.core.userdetails.User(userEntity.getEmail(),
        userEntity.getPass(),
        Collections.singletonList(new SimpleGrantedAuthority(userEntity.getRole())));
  }

}