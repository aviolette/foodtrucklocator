package foodtruck.util;

import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author aviolette
 * @since 4/11/16
 */
public class MoreStringsTest {

  @Test
  public void testCapitalize() throws Exception {
    assertThat(MoreStrings.capitalize("foo bar days")).isEqualTo("Foo Bar Days");
  }

  @Test
  public void testCapitalize1() throws Exception {
    assertThat(MoreStrings.capitalize("foo")).isEqualTo("Foo");
  }

  @Test
  public void testCapitalize2() throws Exception {
    assertThat(MoreStrings.capitalize("")).isEqualTo("");
  }

  @Test
  public void testCapitalize3() throws Exception {
    assertThat(MoreStrings.capitalize("foo   bar days")).isEqualTo("Foo   Bar Days");
  }
}