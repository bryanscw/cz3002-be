package com.qwerty.cogbench.exception;

/**
 * This exception is thrown when user is not authorized.
 * <p>
 * A HTTP 401 status code will be thrown.
 *
 * @author suvoonhou
 */

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnauthorizedException extends RuntimeException {

  public UnauthorizedException() {
    super();
  }

  public UnauthorizedException(String message) {
    super(message);
  }

  public UnauthorizedException(String message, Throwable cause) {
    super(message, cause);
  }

}
