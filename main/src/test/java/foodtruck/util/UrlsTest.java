package foodtruck.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author aviolette
 * @since 3/29/14
 */
public class UrlsTest {

  @Test
  public void stripSessionIdWithQueryString() {
    assertEquals("http://localhost:8080/admin/locations?q=125+S.+Clark%2c+Chicago%2c+IL", Urls.stripSessionId("http://localhost:8080/admin/locations;jsessionid=b8x53ib8k12t?q=125+S.+Clark%2c+Chicago%2c+IL"));
  }

  @Test
  public void stripSessionIdWithNoQueryString() {
    assertEquals("http://localhost:8080/admin/locations", Urls.stripSessionId("http://localhost:8080/admin/locations;jsessionid=b8x53ib8k12t"));
  }

  @Test
  public void stripSessionIdWithNoSessionId() {
    assertEquals("http://localhost:8080/admin/locations?q=125+S.+Clark%2c+Chicago%2c+IL", Urls.stripSessionId("http://localhost:8080/admin/locations?q=125+S.+Clark%2c+Chicago%2c+IL"));
  }

  @Test
  public void stripSessionIdWithNoSessionIdOrQueryString() {
    assertEquals("http://localhost:8080/admin/locations", Urls.stripSessionId("http://localhost:8080/admin/locations"));
  }
}
