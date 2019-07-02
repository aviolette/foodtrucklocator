package foodtruck.model;

/**
 * @author aviolette
 * @since 5/4/16
 */
public class Url {

  private final String url;

  public Url(String url) {
    this.url = url;
  }

  public String getUrl() {
    return url;
  }

  public String getProtocolRelative() {
    if(url.startsWith("http:")) {
      return url.substring(5);
    } else if (url.startsWith("https:")) {
      return url.substring(6);
    }
    return url;
  }

  public String getForDisplay() {
    String displayUrl = removeProtocol();
    if (displayUrl.endsWith("/")) {
      return displayUrl.substring(0, displayUrl.length()-1);
    }
    return displayUrl;
  }

  private String removeProtocol() {
    if (url.startsWith("http://")) {
      return url.substring(7);
    } else if (url.startsWith("https://")) {
      return url.substring(8);
    }
    return url;
  }

  @Override
  public int hashCode() {
    return url.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return url.equals(obj);
  }

  @Override
  public String toString() {
    return url;
  }

  public String secure() {
    return "https:" + getProtocolRelative();
  }
}
