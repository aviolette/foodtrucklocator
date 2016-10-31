package foodtruck.notifications;

import foodtruck.model.NotificationType;

/**
 * @author aviolette
 * @since 2/17/16
 */
public class PushNotification {
  private final String message;
  private final String summary;
  private final NotificationType type;
  private final String token;

  public PushNotification(String summary, String message, String deviceToken, NotificationType type) {
    this.summary = summary;
    this.message = message;
    this.type = type;
    this.token = deviceToken;
  }

  public String getSummary() {
    return this.summary;
  }

  public String getDeviceToken() {
    return this.token;
  }

  public NotificationType getType() {
    return type;
  }

  public String getMessage() {

    return message;
  }
}