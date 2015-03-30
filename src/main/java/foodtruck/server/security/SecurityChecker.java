package foodtruck.server.security;

import javax.ws.rs.WebApplicationException;

/**
 * @author aviolette
 * @since 3/29/15
 */
public interface SecurityChecker {

  /**
   * Throws an exception if the currently logged in user isn't an admin user
   * @throws WebApplicationException
   */
  void requiresAdmin() throws WebApplicationException;

  /**
   * Throws an exception if the currently logged in user is not a manager of the specified truck
   * @param truckId the truck ID
   */
  void requiresLoggedInAs(String truckId) throws WebApplicationException;

  /**
   * Returns true if the currently logged in user is an admin user
   */
  boolean isAdmin();
}
