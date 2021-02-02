package com.qwerty.cogbench.service;

import com.qwerty.cogbench.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

  User create(User user);

  User updateUser(User user);

  boolean delete(String email);

  User get(String email);

  Page<User> fetchAll(Pageable pageable);

}
