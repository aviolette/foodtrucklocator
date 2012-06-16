// Copyright 2012 BrightTag, Inc. All rights reserved.
package foodtruck.server.resources;

/**
 * @author aviolette@gmail.com
 * @since 6/16/12
 */
public class ErrorPayload {
  private final String message;

  public ErrorPayload(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}
