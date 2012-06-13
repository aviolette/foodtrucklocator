package foodtruck.util;

/**
 * @author aviolette@gmail.com
 * @since 3/4/12
 */
public class Link {
  private final String name;
  private final String url;

  public Link(String name, String url) {
    this.name = name;
    this.url = url;
  }

  public String getName() {
    return name;
  }

  public String getUrl() {
    return url;
  }

}
