package foodtruck.server.job;

import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.NotificationDeviceProfileDAO;
import foodtruck.model.NotificationDeviceProfile;
import foodtruck.notifications.NotificationProcessor;
import foodtruck.notifications.PushNotification;

/**
 * @author aviolette
 * @since 2/26/16
 */
@Singleton
public class TestNotificationServlet extends HttpServlet {
  private final NotificationDeviceProfileDAO notificationDAO;
  private final Set<NotificationProcessor> processors;

  @Inject
  public TestNotificationServlet(NotificationDeviceProfileDAO notificationDAO,Set<NotificationProcessor> processors) {
    this.notificationDAO = notificationDAO;
    this.processors = processors;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    for (NotificationDeviceProfile profile : notificationDAO.findAll()) {
      PushNotification pushNotification = new PushNotification("Hello world", "Hello World!", profile.getDeviceToken(), profile.getType());
      for (NotificationProcessor processor : processors) {
        processor.handle(pushNotification);
      }
    }
    /*
    notificationDAO.save(NotificationDeviceProfile.builder()
        .deviceToken("a2b31b4db42feb20804b603055530afa85ea3fb491a649f7df4839379dc47273")
        .locationNames(ImmutableList.of("Wacker and Adams, Chicago, IL"))
        .build());
        */
  }
}
