package foodtruck.server.resources;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

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

  @Inject
  public DeviceRegistrationResource(PushNotificationService service) {
    this.notificationService = service;
  }

  @POST
  public void register(NotificationDeviceProfile profile) {
    this.notificationService.register(profile);
  }
}
