package foodtruck.server.resources;

import java.util.logging.Logger;

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
  private static final Logger log = Logger.getLogger(AuthorizationChecker.class.getName());
  private final ApplicationDAO applicationDAO;

  @Inject
  public AuthorizationChecker(ApplicationDAO applicationDAO) {
    this.applicationDAO = applicationDAO;
  }

  public void requireAppKey(String appKey) throws WebApplicationException {
    if (!Strings.isNullOrEmpty(appKey)) {
      Application app = applicationDAO.findById(appKey);
      if (app != null && app.isEnabled()) {
        log.info("Requesting application: " + app.getName());
        return;
      }
    }
    throw new WebApplicationException(Response.Status.UNAUTHORIZED);
  }

  public void requireAppKeyWithCount(String appKey, long hourlyCount, long dailyCount) throws WebApplicationException {
    if (!Strings.isNullOrEmpty(appKey)) {
      Application app = applicationDAO.findById(appKey);
      if (app != null && app.isEnabled()) {
        if (app.isRateLimitEnabled() && (hourlyCount > 6 || dailyCount > 100)) {
          log.warning("Rate limit exceeded for " + app.getName());
          throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        log.info("Requesting application: " + app.getName());
        return;
      }
    }
    throw new WebApplicationException(Response.Status.UNAUTHORIZED);
  }
}
