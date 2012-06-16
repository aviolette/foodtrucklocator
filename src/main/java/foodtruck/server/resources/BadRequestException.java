// Copyright 2012 BrightTag, Inc. All rights reserved.
package foodtruck.server.resources;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.Responses;

/**
 * @author aviolette@gmail.com
 * @since 6/16/12
 */
public class BadRequestException extends WebApplicationException {
  public BadRequestException(Throwable cause, MediaType mediaType) {
    super(Responses.clientError()
        .entity(new ErrorPayload(cause.getMessage())).type(mediaType).build());
  }

  public BadRequestException(String message) {
    super(Responses.clientError().entity(new ErrorPayload(message))
        .type(MediaType.APPLICATION_JSON_TYPE).build());
  }
}
