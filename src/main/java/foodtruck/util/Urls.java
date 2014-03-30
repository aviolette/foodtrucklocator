package foodtruck.util;

/**
 * @author aviolette
 * @since 3/29/14
 */
public class Urls {
  public static final String stripSessionId(String path) {
    int start = path.indexOf(';'), end = path.indexOf('?');
    if (start == -1) {
      return path;
    } else if (end == -1) {
      return path.substring(0, start);
    }
    return path.substring(0, start) + path.substring(end, path.length());
  }
}
