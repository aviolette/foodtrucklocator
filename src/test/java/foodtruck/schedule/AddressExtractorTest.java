package foodtruck.schedule;

import java.util.List;

import com.google.common.collect.Iterables;

import org.junit.Before;
import org.junit.Ignore;
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
    assertEquals("1601 N. Clark, Chicago, IL", Iterables.getFirst(addresses, null));
  }

  @Test
  public void testParse2() {
    String tweet = "Wed Sched:  HS1: Monroe&Dearborn  HS2: Monroe&Wacker.  11:30am start times.  Waka Waka Chakalaka!!";
    List<String> addresses = parser.parse(tweet);
    assertNotNull(addresses);
    assertEquals(2, addresses.size());
    assertEquals("Monroe&Dearborn, Chicago, IL", addresses.get(0));
    assertEquals("Monroe&Wacker, Chicago, IL", addresses.get(1));
  }

  @Test
  public void testParse3() {
    String tweet = "Wednesday...\n" +
        "11:30 Dearborn/Monroe\n" +
        "1:30 Monroe/Wacker";
    List<String> addresses = parser.parse(tweet);
    assertNotNull(addresses);
    assertEquals(2, addresses.size());
    assertEquals("Dearborn/Monroe, Chicago, IL", addresses.get(0));
    assertEquals("Monroe/Wacker, Chicago, IL", addresses.get(1));
  }

  @Test
  public void testParse4() {
    String tweet = "Dearborn and Monroe Ashland and Paulina.";
    List<String> addresses = parser.parse(tweet);
    assertNotNull(addresses);
    assertEquals(2, addresses.size());
    assertEquals("Dearborn and Monroe, Chicago, IL", addresses.get(0));
    assertEquals("Ashland and Paulina, Chicago, IL", addresses.get(1));
  }

  @Test
  public void testParseIntersection() {
    String tweet = "Just landed at Rush and Walton foobar";
    List<String> addresses = parser.parse(tweet);
    assertNotNull(addresses);
    assertEquals(1, addresses.size());
    assertEquals("Rush and Walton, Chicago, IL", addresses.get(0));
  }

 @Test
  public void testParseIntersection2() {
    String tweet = "Gold Coast, we have landed at Rush and Walton...here until 6 pm";
    List<String> addresses = parser.parse(tweet);
    assertNotNull(addresses);
    assertEquals(1, addresses.size());
    assertEquals("Rush and Walton, Chicago, IL", addresses.get(0));
  }

 @Test
  public void testParseIntersection3() {
    String tweet = "Thanks Gold Coast! River North you ready?  Kingsbury and Erie, here we come!";
    assertTweet("Kingsbury and Erie, Chicago, IL", tweet);
  }

  public void testParseIntersection4() {
    assertTweet("Hubbard and Lasalle, Chicago, IL", "Alright #RiverNorth we're at Hubbard and LaSalle for the final stop of the night");
  }

  private void assertTweet(String expected, String tweet) {
    List<String> addresses = parser.parse(tweet);
    assertNotNull(addresses);
    assertEquals(1, addresses.size());
    assertEquals(expected, addresses.get(0));
  }
}
