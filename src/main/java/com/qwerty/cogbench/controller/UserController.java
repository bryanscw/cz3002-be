package com.qwerty.cogbench.controller;

import com.qwerty.cogbench.model.User;
import com.qwerty.cogbench.service.UserService;
import java.security.Principal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(
    value = {"/users"},
    produces = MediaType.APPLICATION_JSON_VALUE
)
@Validated
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  /**
   * Get user details.
   *
   * @param principal Principal context containing information of the user submitting the request
   * @return Created user
   */
  @RequestMapping(method = RequestMethod.POST, path = "/me")
  @Secured({})
  @ResponseStatus(HttpStatus.OK)
  public User getUser(Principal principal) {
    log.info("Getting details for user [{}]", principal.getName());
    return userService.get(principal.getName());
  }

  /**
   * Fetch all user details.
   *
   * @return Paginated result of all users
   */
  @RequestMapping(method = RequestMethod.GET, path = "/")
  @Secured({"ROLE_ADMIN"})
  @ResponseStatus(HttpStatus.OK)
  public Iterable<User> fetchAllUsers() {
    log.info("Fetching all user details");
    return userService.fetchAll();
  }

  /**
   * Create a new user.
   *
   * @param user User to be created
   * @return Created user
   */
  @RequestMapping(method = RequestMethod.POST, path = "/create")
  @Secured({"ROLE_ADMIN"})
  @ResponseStatus(HttpStatus.OK)
  public User createUser(@RequestBody User user) {
    log.info("Creating user [{}] with role [{}]", user.getEmail(), user.getRole());
    return userService.create(user);
  }

  /**
   * Update a User.
   *
   * @param userEmail email that User is referenced by
   * @param user      User to be updated
   * @return Updated user
   */
  @RequestMapping(method = {RequestMethod.PUT, RequestMethod.PATCH}, path = "/{userEmail}")
  @ResponseStatus(HttpStatus.OK)
  @Secured({"ROLE_ADMIN"})
  public User updateUser(@PathVariable(value = "userEmail") String userEmail,
      @RequestBody User user) {
    log.info("Updating user with email: [{}]", userEmail);
    user.setEmail(userEmail);
    return userService.updateUser(user);
  }

  /**
   * Delete a User.
   * <p>
   *
   * @param userEmail Email of user (Email is used as the primary key
   * @return Flag indicating if request is successful
   */
  @RequestMapping(method = RequestMethod.DELETE, path = "/{userEmail}")
  @ResponseStatus(HttpStatus.OK)
  @Secured({"ROLE_ADMIN"})
  public boolean deleteUser(@PathVariable(value = "userEmail") String userEmail) {
    log.info("Deleting user with email: [{}]", userEmail);
    return userService.delete(userEmail);
  }

}
