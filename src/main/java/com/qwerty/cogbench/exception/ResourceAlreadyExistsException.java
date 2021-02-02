package com.qwerty.cogbench.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * This exception is thrown when resource already exists
 * <p>
 * A HTTP 400 status code will be thrown.
 *
 * @author suvoonhou
 */

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ResourceAlreadyExistsException extends RuntimeException {

  public ResourceAlreadyExistsException() {
    super();
  }

  public ResourceAlreadyExistsException(String message) {
    super(message);
  }

  public ResourceAlreadyExistsException(String message, Throwable cause) {
    super(message, cause);
  }

}
