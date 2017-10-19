package foodtruck.user;

import java.io.Serializable;
import java.security.Principal;

import javax.security.auth.Subject;

import com.google.common.base.MoreObjects;

/**
 * @author aviolette
 * @since 12/6/16
 */
public class LoggedInUser implements Principal, Serializable {
  private final String name;
  private final boolean admin;

  public LoggedInUser(String name, boolean isAdmin) {
    this.name = name;
    this.admin = isAdmin;
  }

  public boolean isAdmin() {
    return this.admin;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("name", name)
        .add("admin", admin)
        .toString();
  }

  @Override
  public String getName() {
    return name;
  }

  public boolean implies(Subject subject) {
    return false;
  }
}
