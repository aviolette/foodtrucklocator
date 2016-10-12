package foodtruck.util;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * @author aviolette
 * @since 6/18/14
 */
class SessionIdentifier {
  private SecureRandom random = new SecureRandom();

  String nextSessionId() {
    return new BigInteger(130, random).toString(32);
  }
}
