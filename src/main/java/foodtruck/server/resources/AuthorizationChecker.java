package foodtruck.server.resources;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.google.common.base.Strings;
import com.google.inject.Inject;

import foodtruck.dao.ApplicationDAO;
import foodtruck.model.Application;

/**
 * Contains logic for
 * @author aviolette
 * @since 3/6/13
 */
public class AuthorizationChecker {
  private final ApplicationDAO applicationDAO;

  @Inject
  public AuthorizationChecker(ApplicationDAO applicationDAO) {
    this.applicationDAO = applicationDAO;
  }

  public void requireAppKey(String appKey) throws WebApplicationException {
    if (!Strings.isNullOrEmpty(appKey)) {
      Application app = applicationDAO.findById(appKey);
      if (app != null && app.isEnabled()) {
        return;
      }
    }
    throw new WebApplicationException(Response.Status.UNAUTHORIZED);
  }
}
