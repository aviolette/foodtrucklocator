package foodtruck.user;

import java.security.Principal;

import javax.security.auth.Subject;

/**
 * @author aviolette
 * @since 12/6/16
 */
public class LoggedInUser implements Principal {
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
  public String getName() {
    return name;
  }

  public boolean implies(Subject subject) {
    return false;
  }
}
