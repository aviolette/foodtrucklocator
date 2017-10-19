package foodtruck.model;


import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author aviolette
 * @since 10/30/16
 */
public class LocationTest {
  @Test
  public void getShortenedName() {
    Location location = Location.builder()
        .name("226 N Clinton St, Chicago, IL 60661, USA")
        .build();
    assertThat(location.getShortenedName()).isEqualTo("226 N Clinton St");
  }
}