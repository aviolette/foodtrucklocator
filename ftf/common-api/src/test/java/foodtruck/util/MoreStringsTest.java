package foodtruck.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author aviolette
 * @since 4/11/16
 */
public class MoreStringsTest {

  @Test
  public void testCapitalize() throws Exception {
    assertEquals("Foo Bar Days", MoreStrings.capitalize("foo bar days"));
  }

  @Test
  public void testCapitalize1() throws Exception {
    assertEquals("Foo", MoreStrings.capitalize("foo"));
  }

  @Test
  public void testCapitalize2() throws Exception {
    assertEquals("", MoreStrings.capitalize(""));
  }

  @Test
  public void testCapitalize3() throws Exception {
    assertEquals("Foo   Bar Days", MoreStrings.capitalize("foo   bar days"));
  }
}