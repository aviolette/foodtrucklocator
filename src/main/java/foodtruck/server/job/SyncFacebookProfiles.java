package foodtruck.server.job;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.facebook.FacebookService;

/**
 * @author aviolette
 * @since 2/18/13
 */
@Singleton
public class SyncFacebookProfiles extends HttpServlet {
  private final FacebookService service;

  @Inject
  public SyncFacebookProfiles(FacebookService service) {
    this.service = service;
  }

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    service.syncTruckData();
  }
}
