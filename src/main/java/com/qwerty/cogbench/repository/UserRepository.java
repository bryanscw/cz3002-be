package com.qwerty.cogbench.repository;

import com.qwerty.cogbench.model.User;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {

  Optional<User> findUserByEmail(String email);

  boolean existsUserByEmail(String email);

  Optional<List<User>> findAllByRole(List<User> list, String role);

}