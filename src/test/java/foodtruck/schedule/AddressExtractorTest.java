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
  public void testParse5() {
    String tweet = "555w Kenzie your cupcakes are here! It sure is a great day for a cupcake!";
    List<String> addresses = parser.parse(tweet);
    assertNotNull(addresses);
    assertEquals(1, addresses.size());
    assertEquals("555w Kenzie, Chicago, IL", addresses.get(0));
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

  @Test
  public void testParseIntersection4() {
    assertTweet("Hubbard and LaSalle, Chicago, IL", "Alright #RiverNorth we're at Hubbard and LaSalle for the final stop of the night");
  }

  @Test
  public void testParseIntersection5() {
    assertTweet("North and Wells, Chicago, IL", "Last call at North and Wells for the #KefirTruck! T-10 minutes!");
    assertTweet("Michigan and Ohio, Chicago, IL", "Oh yea oh yea beautiful night in the Chi. Come get ur froyo fix we are on the corner of Michigan and Ohio!");
  }

  @Test
  public void testKeyword() {
    assertTweet("Harpo Studios", "Just landed at harpo.  Come get your yogurt");
    assertTweet("Harpo Studios", "Just landed at Harpo.  Come get your yogurt");
    assertTweet("Harpo Studios", "Just landed at Harpo Studios.  Come get your yogurt");
    List<String> addresses = parser.parse("Come see @MamaGreenGoodie at the Men's Health Magazine Urbanathalon tomorrow at Grant Park. We will be parked on Columbus & Balbo.");
    assertNotNull(addresses);
    assertEquals(2, addresses.size());
    assertEquals("Columbus and Balbo, Chicago, IL", addresses.get(0));
    assertEquals("Grant Park", addresses.get(1));
  }

  @Test
  public void testTamaleSpaceshipFormat() {
    assertTweet("Adler Planetarium", "Going strong at <<Adler Planetarium>> for \"Jenny & Mike's\" Wedding ;)");
  }

  @Test
  public void testFoursquareFormat() {
    assertTweet("694 Wine & Spirits", "Time for Wagyu! Come eat (@ 694 Wine & Spirits) http://t.co/mwKr61G6");
  }


  private void assertTweet(String expected, String tweet) {
    List<String> addresses = parser.parse(tweet);
    assertNotNull(addresses);
    assertEquals(1, addresses.size());
    assertEquals(expected, addresses.get(0));
  }
}
