package foodtruck.alexa;

/**
 * @author aviolette
 * @since 1/19/18
 */
public class DeviceAccess {

  private final String deviceId;
  private final String accessToken;

  public DeviceAccess(String deviceId, String accessToken) {
    this.deviceId = deviceId;
    this.accessToken = accessToken;
  }

  public String getDeviceId() {
    return deviceId;
  }

  public String getAccessToken() {
    return accessToken;
  }
}
