package foodtruck.util;

/**
 * @author aviolette@gmail.com
 * @since 7/28/12
 */
public class MoreStrings {
  public static String capitalize(String s) {
    if(s.length() < 2) {
      return s.toUpperCase();
    }
    return s.substring(0,1).toUpperCase() + s.substring(1);
  }
}
