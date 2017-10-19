package foodtruck.util;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author aviolette
 * @since 3/29/14
 */
public class UrlsTest {

  @Test
  public void stripSessionIdWithQueryString() {
    assertThat(Urls.stripSessionId("http://localhost:8080/admin/locations;jsessionid=b8x53ib8k12t?q=125+S.+Clark%2c+Chicago%2c+IL")).isEqualTo("http://localhost:8080/admin/locations?q=125+S.+Clark%2c+Chicago%2c+IL");
  }

  @Test
  public void stripSessionIdWithNoQueryString() {
    assertThat(Urls.stripSessionId("http://localhost:8080/admin/locations;jsessionid=b8x53ib8k12t")).isEqualTo("http://localhost:8080/admin/locations");
  }

  @Test
  public void stripSessionIdWithNoSessionId() {
    assertThat(Urls.stripSessionId("http://localhost:8080/admin/locations?q=125+S.+Clark%2c+Chicago%2c+IL")).isEqualTo("http://localhost:8080/admin/locations?q=125+S.+Clark%2c+Chicago%2c+IL");
  }

  @Test
  public void stripSessionIdWithNoSessionIdOrQueryString() {
    assertThat(Urls.stripSessionId("http://localhost:8080/admin/locations")).isEqualTo("http://localhost:8080/admin/locations");
  }
}
