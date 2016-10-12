package foodtruck.server.job;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.util.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.socialmedia.ProfileSyncService;

/**
 * @author aviolette
 * @since 2/10/15
 */
@Singleton
public class ProfileSyncServlet extends HttpServlet {
  private final ProfileSyncService service;

  @Inject
  public ProfileSyncServlet(ProfileSyncService service) {
    this.service = service;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String truckId = req.getParameter("truckId");
    if (Strings.isNullOrEmpty(truckId)) {
      service.syncAllProfiles();
    } else {
      service.syncProfile(truckId);
    }
  }
}
