package com.qwerty.cogbench.service;

import com.qwerty.cogbench.exception.ResourceAlreadyExistsException;
import com.qwerty.cogbench.exception.ResourceNotFoundException;
import com.qwerty.cogbench.model.User;
import com.qwerty.cogbench.repository.UserRepository;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[ayb]\\$.{56}$");

  public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public User create(User user) {
    if (userRepository.existsUserByEmail(user.getEmail())) {
      log.error("User with email [{}] is already in use", user.getEmail());
      throw new ResourceAlreadyExistsException(
          String.format("User with email [%s] is already in use", user.getEmail()));
    }

    String encryptedPassword = encryptPassword(user.getPass());
    user.setPass(encryptedPassword);

    return userRepository.save(user);
  }

  @Override
  public User updateUser(User user) {
    User userToFind = userRepository.findById(user.getEmail()).orElseThrow(() -> {
      String errorMsg = String.format("User with email: [%s] not found", user.getEmail());
      log.error(errorMsg);
      throw new ResourceNotFoundException(errorMsg);
    });

    if (user.getEmail() != null) {
      userToFind.setEmail(user.getEmail());
    }

    if (user.getName() != null) {
      userToFind.setName(user.getName());
    }

    if (user.getRole() != null) {
      userToFind.setRole(user.getRole());
    }

    if (user.getPass() != null) {
      userToFind.setPass(encryptPassword(user.getPass()));
    }

    return userRepository.save(userToFind);
  }

  @Override
  public boolean delete(String email) {
    userRepository.findUserByEmail(email).orElseThrow(() -> new ResourceNotFoundException(
        String.format("User with email [%s] not found", email)));
    userRepository.deleteById(email);
    return true;
  }

  @Override
  public User get(String email) {
    return userRepository.findUserByEmail(email).orElseThrow(
        () -> new ResourceNotFoundException(
            String.format("User with email [%s] not found", email)));
  }

  @Override
  public Page<User> fetchAll(Pageable pageable) {
    return userRepository.findAll(pageable);
  }

  @Override
  public Page<User> fetchAllPatients(Pageable pageable, String role) {
    return userRepository.findAllByRole(pageable, role).orElseThrow(
          () -> new ResourceNotFoundException(
                  String.format("Users with role [%s] not found", role)));
  }

  /**
   * Checks if a password is Bcrypt crypted. It will Bcrypt encrypt the password if it is not.
   *
   * @param password Password, can be in plaintext or Bcrypt crypted
   * @return Bcrypt encrypted password
   */
  private String encryptPassword(String password) {
    // Check if password is encrypted
    if (!BCRYPT_PATTERN.matcher(password).matches()) {
      // Encrypt the password
      return passwordEncoder.encode(password);
    }
    // Password is already Bcrypt crypted
    return password;
  }

}
