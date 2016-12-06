package foodtruck.web.services.v3;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

/**
 * @author aviolette
 * @since 12/5/16
 */
@Provider
public class JacksonProvider implements ContextResolver<ObjectMapper> {
  private final ObjectMapper objectMapper;

  @Inject
  public JacksonProvider(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public ObjectMapper getContext(Class<?> type) {
    return objectMapper;
  }
}
