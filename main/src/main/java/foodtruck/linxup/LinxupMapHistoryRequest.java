package foodtruck.linxup;

import com.google.common.base.MoreObjects;

import org.joda.time.DateTime;

/**
 * @author aviolette
 * @since 10/29/16
 */
public class LinxupMapHistoryRequest {
  private final String userName;
  private final String password;
  private final DateTime start;
  private final DateTime end;
  private final String deviceId;

  LinxupMapHistoryRequest(String userName, String password, DateTime start, DateTime end, String deviceId) {
    this.password = password;
    this.userName = userName;
    this.start = start;
    this.end = end;
    this.deviceId = deviceId;
  }

  public String getDeviceId() {
    return deviceId;
  }

  public DateTime getEnd() {
    return end;
  }

  public DateTime getStart() {
    return start;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("username", userName)
        .add("password", password)
        .add("start", start)
        .add("end", end)
        .toString();
  }

  String getUserName() {
    return userName;
  }

  String getPassword() {
    return password;
  }

}
