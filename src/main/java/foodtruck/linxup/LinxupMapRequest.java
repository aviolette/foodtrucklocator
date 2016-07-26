package foodtruck.linxup;

import com.google.common.base.MoreObjects;

/**
 * @author aviolette
 * @since 7/25/16
 */
class LinxupMapRequest {
  private final String userName;
  private final String password;

  LinxupMapRequest(String userName, String password) {
    this.password = password;
    this.userName = userName;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("username", userName)
        .add("password", password)
        .toString();
  }

  String getUserName() {
    return userName;
  }

  String getPassword() {
    return password;
  }
}
