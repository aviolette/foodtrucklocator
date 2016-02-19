package foodtruck.server.resources;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import com.google.inject.Inject;

import foodtruck.model.NotificationDeviceProfile;
import foodtruck.notifications.PushNotificationService;

/**
 * Registers a device to receive push notifications.
 * @author aviolette
 * @since 2/16/16
 */
@Path("/device_registration")
public class DeviceRegistrationResource {
  private final PushNotificationService notificationService;
  private final AuthorizationChecker checker;

  @Inject
  public DeviceRegistrationResource(PushNotificationService service, AuthorizationChecker checker) {
    this.notificationService = service;
    this.checker = checker;
  }

  @POST
  public void register(@QueryParam("appKey") final String appKey, NotificationDeviceProfile profile) {
    if (checker.canRegisterForNotifications(appKey)) {
      this.notificationService.register(profile);
    }
  }
}
