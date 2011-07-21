package foodtruck.schedule;

import java.util.List;

import com.google.common.collect.Iterables;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author aviolette@gmail.com
 * @since Jul 20, 2011
 */
public class AddressExtractorTest {
  private AddressExtractor parser;

  @Before
  public void before() {
    parser = new AddressExtractor();
  }

  @Test
  public void testParse1() {
    String tweet = "We're in a food truck flashmob at 4PM today across from 1601 N. Clark! Hello #HotFreshHistory www.chicagohs.org";
    List<String> addresses = parser.parse(tweet);
    assertNotNull(addresses);
    assertEquals(1, addresses.size());
    assertEquals("1601 N. Clark", Iterables.getFirst(addresses, null));
  }

  @Test
  public void testParse2() {
    String tweet = "Wed Sched:  HS1: Monroe&Dearborn  HS2: Monroe&Wacker.  11:30am start times.  Waka Waka Chakalaka!!";
    List<String> addresses = parser.parse(tweet);
    assertNotNull(addresses);
    assertEquals(2, addresses.size());
    assertEquals("Monroe&Dearborn", addresses.get(0));
    assertEquals("Monroe&Wacker", addresses.get(1));
  }

  @Test
  public void testParse3() {
    String tweet = "Wednesday...\n" +
        "11:30 Dearborn/Monroe\n" +
        "1:30 Monroe/Wacker";
    List<String> addresses = parser.parse(tweet);
    assertNotNull(addresses);
    for (String s : addresses) {
      System.out.println("FOO: "+s);
    }
    assertEquals(2, addresses.size());
    assertEquals("Dearborn/Monroe", addresses.get(0));
    assertEquals("Monroe/Wacker", addresses.get(1));
  }

  @Test
  public void testParse4() {
    String tweet = "Dearborn and Monroe Ashland and Paulina.";
    List<String> addresses = parser.parse(tweet);
    assertNotNull(addresses);
    assertEquals(2, addresses.size());
    assertEquals("Dearborn and Monroe", addresses.get(0));
    assertEquals("Ashland and Paulina", addresses.get(1));
  }
}
