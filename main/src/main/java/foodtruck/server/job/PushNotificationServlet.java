package foodtruck.server.job;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.notifications.PushNotificationService;

/**
 * @author aviolette
 * @since 2/17/16
 */
@Singleton
public class PushNotificationServlet extends HttpServlet {
  private final PushNotificationService service;

  @Inject
  public PushNotificationServlet(PushNotificationService service) {
    this.service = service;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    service.sendPushNotifications();
  }
}
