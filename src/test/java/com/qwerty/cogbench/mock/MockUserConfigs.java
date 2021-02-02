package com.qwerty.cogbench.mock;

import java.util.Collections;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@TestConfiguration
public class MockUserConfigs {

  @Bean
  @Primary
  public UserDetailsService userDetailsService() {

    // Creating a user with the no roles
    User basicUser = new org.springframework.security.core.userdetails.User(
        "user1@test.com",
        "password",
        Collections.emptyList());

    // Creating a user with the CANDIDATE role
    User candidate = new org.springframework.security.core.userdetails.User(
        "candidate1@test.com",
        "password",
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_CAND")));

    // Creating a user with the ADMIN role
    User admin = new org.springframework.security.core.userdetails.User(
        "admin1@test.com",
        "password",
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN")));

    // Creating a user with the PROFESSIONAL role
    User professional = new org.springframework.security.core.userdetails.User(
        "professional1@test.com",
        "password",
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_PROF")));

    // Creating a user with the PROFESSIONAL role
    User professional2 = new org.springframework.security.core.userdetails.User(
        "professional2@test.com",
        "password",
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_PROF")));

    return new InMemoryUserDetailsManager(basicUser, candidate, admin, professional, professional2);
  }

}
