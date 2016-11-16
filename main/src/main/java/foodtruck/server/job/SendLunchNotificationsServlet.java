package foodtruck.server.job;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.notifications.PublicEventNotificationService;

/**
 * @author aviolette
 * @since 12/3/12
 */
@Singleton
public class SendLunchNotificationsServlet extends HttpServlet {
  private static final Logger log = Logger.getLogger(SendLunchNotificationsServlet.class.getName());
  private final PublicEventNotificationService notificationService;

  @Inject
  public SendLunchNotificationsServlet(PublicEventNotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    log.info("Sending out notifications...");
    notificationService.sendLunchtimeNotifications();
  }
}
