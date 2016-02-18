package foodtruck.notifications;

import foodtruck.model.NotificationDeviceProfile;

/**
 * @author aviolette
 * @since 2/14/16
 */
public interface PushNotificationService {

  void sendPushNotifications();

  void register(NotificationDeviceProfile profile);

  void deregister(String deviceToken);
}
