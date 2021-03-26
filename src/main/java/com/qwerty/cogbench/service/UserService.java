package com.qwerty.cogbench.service;

import com.qwerty.cogbench.model.User;

import java.util.List;

public interface UserService {

  User create(User user);

  User updateUser(User user);

  boolean delete(String email);

  User get(String email);

  List<User> fetchAll();

  List<User> fetchAllPatients(String role);

}
