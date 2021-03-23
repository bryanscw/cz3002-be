package com.qwerty.cogbench.repository;

import com.qwerty.cogbench.model.User;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface UserRepository extends PagingAndSortingRepository<User, String> {

  Optional<User> findUserByEmail(String email);

  boolean existsUserByEmail(String email);

  Optional<Page<User>> findAllByRole(Pageable pageable, String role);

}