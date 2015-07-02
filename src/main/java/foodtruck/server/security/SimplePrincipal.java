package foodtruck.server.security;

import java.io.Serializable;
import java.security.Principal;

/**
 * A basic implementation of Principal
 * @author aviolette
 * @since 7/1/15
 */
public class SimplePrincipal implements Principal, Serializable {
  private final String name;

  public SimplePrincipal(String name) {
    this.name = name;
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else if (!(obj instanceof SimplePrincipal)) {
      return false;
    }
    SimplePrincipal that = (SimplePrincipal) obj;
    return name.equals(that.getName());
  }

  @Override
  public String getName() {
    return name;
  }
}
