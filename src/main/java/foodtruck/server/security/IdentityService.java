package foodtruck.server.security;

import java.security.Principal;

/**
 * @author aviolette
 * @since 7/1/15
 */
public interface IdentityService {
  Principal getCurrentUser();
}
