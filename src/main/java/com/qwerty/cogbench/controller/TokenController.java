package com.qwerty.cogbench.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(
    value = {"/oauth"},
    produces = MediaType.APPLICATION_JSON_VALUE
)
@Validated
public class TokenController {

  private final DefaultTokenServices tokenServices;

  public TokenController(DefaultTokenServices tokenServices) {
    this.tokenServices = tokenServices;
  }

  /**
   * Revoke the existing access and refresh token.
   * <p>
   * This can be used whenever a user wants to logout and wants all existing access and refresh
   * tokens that are still valid to be invalidated.
   *
   * @param authentication Authentication context containing information of the user submitting the
   *                       request
   */
  @RequestMapping(method = RequestMethod.DELETE, path = "/revoke")
  @ResponseStatus(HttpStatus.OK)
  public void revokeToken(Authentication authentication) {
    final String userToken = ((OAuth2AuthenticationDetails) authentication.getDetails())
        .getTokenValue();
    log.info("Revoking token: [{}]", userToken);
    tokenServices.revokeToken(userToken);
  }

}