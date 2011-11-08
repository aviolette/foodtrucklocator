package foodtruck.schedule;

import java.util.List;

import com.google.common.collect.Iterables;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import foodtruck.model.Truck;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author aviolette@gmail.com
 * @since Jul 20, 2011
 */
public class AddressExtractorTest {
  private AddressExtractor parser;
  private Truck truck;

  @Before
  public void before() {
    parser = new AddressExtractor();
    truck = new Truck.Builder().build();
  }

  @Test
  public void testParse1() {
    String tweet =
        "We're in a food truck flashmob at 4PM today across from 1601 N. Clark! Hello #HotFreshHistory www.chicagohs.org";
    List<String> addresses = parser.parse(tweet, truck);
    assertNotNull(addresses);
    assertEquals(1, addresses.size());
    assertEquals("1601 N. Clark, Chicago, IL", Iterables.getFirst(addresses, null));
  }

  @Test
  public void testParse2() {
    String tweet =
        "Wed Sched:  HS1: Monroe&Dearborn  HS2: Monroe & Wacker.  11:30am start times.  Waka Waka Chakalaka!!";
    List<String> addresses = parser.parse(tweet, truck);
    assertNotNull(addresses);
    assertEquals(2, addresses.size());
    assertEquals("Monroe and Dearborn, Chicago, IL", addresses.get(0));
    assertEquals("Monroe and Wacker, Chicago, IL", addresses.get(1));
  }

  @Test
  public void testParse3() {
    String tweet = "Wednesday...\n" +
        "11:30 Dearborn/Monroe\n" +
        "1:30 Monroe/Wacker";
    List<String> addresses = parser.parse(tweet, truck);
    assertNotNull(addresses);
    assertEquals(2, addresses.size());
    assertEquals("Dearborn and Monroe, Chicago, IL", addresses.get(0));
    assertEquals("Monroe and Wacker, Chicago, IL", addresses.get(1));
  }

  @Test
  public void testParse4() {
    String tweet = "Dearborn and Monroe Ashland and Paulina.";
    List<String> addresses = parser.parse(tweet, truck);
    assertNotNull(addresses);
    assertEquals(2, addresses.size());
    assertEquals("Dearborn and Monroe, Chicago, IL", addresses.get(0));
    assertEquals("Ashland and Paulina, Chicago, IL", addresses.get(1));
  }

  @Test
  public void testParse5() {
    String tweet = "555w Kenzie your cupcakes are here! It sure is a great day for a cupcake!";
    List<String> addresses = parser.parse(tweet, truck);
    assertNotNull(addresses);
    assertEquals(1, addresses.size());
    assertEquals("555w Kenzie, Chicago, IL", addresses.get(0));
  }

  @Test
  public void testParseFuckedUpAddress() {
    assertTweet("321 S Wacker, Chicago, IL",
        "Landed! 321 S Wacker (at Van Buren) near Willis Tower. Pulled pork chicken sammiches, coleslaw or baked beans, & sauce for $10! Cash only!");
  }

  @Test
  public void testParse6() {
    assertTweet("600 W. Chicago, Chicago, IL", "600 W. Chicago. En route.");
  }

  @Test
  public void testParseIntersection() {
    String tweet = "Just landed at Rush and Walton foobar";
    List<String> addresses = parser.parse(tweet, truck);
    assertNotNull(addresses);
    assertEquals(1, addresses.size());
    assertEquals("Rush and Walton, Chicago, IL", addresses.get(0));
  }

  @Test
  public void testParseIntersection2() {
    String tweet = "Gold Coast, we have landed at Rush and Walton...here until 6 pm";
    List<String> addresses = parser.parse(tweet, truck);
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
    assertTweet("Hubbard and LaSalle, Chicago, IL",
        "Alright #RiverNorth we're at Hubbard and LaSalle for the final stop of the night");
  }

  @Test
  public void testParseIntersectionWithAtNotation() {
    assertTweet("Wabash and Ohio, Chicago, IL",
        "The PURPLE Bus scooted on down Wabash at Ohio!");
    assertTweet("Halsted and Belmont, Chicago, IL", "#KefirTruck on Halsted at Belmont!");
  }

  @Test
  public void testWttw() {
    assertTweet("WTTW",
        "Ok we'll be @wttw @neiulife for 10 more minutes then heading over to @Grubhub. Who wants some duck gumbo?");
  }

  @Test
  public void testParseIntersection5() {
    assertTweet("North and Wells, Chicago, IL",
        "Last call at North and Wells for the #KefirTruck! T-10 minutes!");
    assertTweet("Michigan and Ohio, Chicago, IL",
        "Oh yea oh yea beautiful night in the Chi. Come get ur froyo fix we are on the corner of Michigan and Ohio!");
  }

  @Test
  public void testOnlyParseMixedCasedIntersections() {
    assertTweet("Randolph and Columbus, Chicago, IL",
        "Original tart, carrot cake, and cake batter today! Come and get it! Just arrived at Randolph & Columbus.");
    assertTweet("13th and Michigan, Chicago, IL",
        "Original tart, carrot cake, and cake batter today! Come and get it! Just arrived at 13th and Michigan.");
    assertTweet("13th and S Michigan, Chicago, IL",
        "Bring your Sweet Tooth to 13th / S Michigan.  Sold the last Pumpkin Latte but still have Red Velvet, Choco Cocoa,... http://t.co/PtDKjth0");
  }

  @Test
  public void testIntersection6() {
    assertTweet("Kingsbury and Erie, Chicago, IL",
        "StarfruitCafe: The #KefirTruck is @Kingsbury and Erie! Come enjoy some frozen kefir and some great weather! Flavors are strawberry and original.");
  }

  @Test
  public void testIntersection7() {
    assertTweet("Aberdeen and Randolph, Chicago, IL",
        "TheWagyuWagon: At corner of Aberdeen n Randolph ready to serve!");
  }

  @Test
  public void testIntersection8() {
    assertTweet("Kingsbury and Erie, Chicago, IL", "#KefirTruck at Kingsbury and Erie!");
  }

  @Test
  public void testJeffJack() {
    assertTweet("Jefferson and Jackson, Chicago, IL",
        "CourageousCakes: Ok Jeff/jack finally here! #greenmachine moves a bit slower in the rain!");
  }

  @Test
  public void testContextSpecificLocation() {
    assertTweet("834 Lake St, Oak Park, IL",
        "hello everyone, mj express is by the library.  comey by",
        new Truck.Builder().id("mjexpress14").defaultCity("Oak Park, IL").build());
  }

  @Test
  public void testKeyword() {
    assertTweet("Harpo Studios", "Just landed at harpo.  Come get your yogurt");
    assertTweet("Harpo Studios", "Just landed at Harpo.  Come get your yogurt");
    assertTweet("Harpo Studios", "Just landed at Harpo Studios.  Come get your yogurt");
    List<String> addresses = parser.parse(
        "Come see @MamaGreenGoodie at the Men's Health Magazine Urbanathalon tomorrow at Grant Park. We will be parked on Columbus & Balbo.",
        truck);
    assertNotNull(addresses);
    assertEquals(2, addresses.size());
    assertEquals("Columbus and Balbo, Chicago, IL", addresses.get(0));
    assertEquals("Grant Park", addresses.get(1));
  }

  @Test
  public void testBetweenTwoStreets() {
    assertTweet("Madison and Dearborn, Chicago, IL",
        "BaoMouth: Chase Tower, on Madison between Dearborn & Clark, the BAO-Mobile is heading your way shortly #BunsOnTheRun");
    assertTweet("Huron and Fairbanks, Chicago, IL",
        "FossFoodTrucks: Meatyballs have landed on Huron between Fairbanks & Mcclurg. Come and grab some balls for the next 25 minutes.");
    assertTweet("Chicago and Franklin, Chicago, IL",
        "stemartaen: Chicago and Franklin between 1:20 and 1:30 today.  #veganfoodtruck");
    assertTweet("Chicago and Fairbanks, Chicago, IL",
        "We're on Chicago b/w Fairbanks and LSD. @punkrawk82 @ashesbodashes @jketay @deitranotdietra @chifoodtruckgal");
    assertTweet("Erie and Franklin, Chicago, IL",
        "Hey all! I'm hanging in Erie in between Franklin and Orleans.  Let's party...its monday!");
    assertTweet("Wabash and Van Buren, Chicago, IL",
        "thesouthernmac: Alright folks, sorry, we're on Wabash between Van Buren and Jackson!");
  }

  @Test
  public void testAlternateBetweenFormat() {
    assertTweet("Orleans and Kinzie, Chicago, IL", "Landed between Franklin and Orleans on Kinzie");
  }

  @Test
  public void testNear() {
    assertTweet("Superior and Lawndale, Chicago, IL",
        "Last call for some delicious in #Streeterville. Leaving at 2:30. Hurry over to Superior near Lawndale");
  }

  @Test
  public void testSearsTower() {
    assertTweet("Wacker and Adams, Chicago, IL",
        "BergsteinsNY: Sears/Willis today! Soup, sandwich, kugel, cabbage rolls!");
  }

  @Test
  public void testTrumpTower() {
    assertTweet("400 North Wabash, Chicago, IL",
        "The BAO-Mobile is at Trump Tower. #fb #BunsOnTheRun");
  }

  @Test
  public void testTamaleSpaceshipFormat() {
    assertTweet("Adler Planetarium",
        "Going strong at <<Adler Planetarium>> for \"Jenny & Mike's\" Wedding ;)");
  }

  @Test
  public void testFoursquareFormat() {
    assertTweet("694 Wine & Spirits",
        "Time for Wagyu! Come eat (@ 694 Wine & Spirits) http://t.co/mwKr61G6");
  }

  @Test
  public void testFoursquareFormatWithUserCount() {
    assertTweet("Soldier Field",
        "Family walk (@ Soldier Field w/ 2 others) [pic]: http://t.co/BJJjbmQt");
  }

  @Test
  public void testUofC() {
    assertTweet("57th and Ellis, Chicago, IL",
        "Thank you U of Chicago for braving the weather today and South Loop for closing out our day!  Enjoy the rest of your night!");
    assertTweet("57th and Ellis, Chicago, IL",
        "Another RAINY Day.. GiGi is making usual stop at UIC today. Ellis 57 & 58 11:30.  Ellis / 60th 1:30 @UofCGHI @UChicago @uicradio @uchiNOMgo");
  }

  @Test
  public void testUIC() {
    assertTweet("Vernon Park Circle, Chicago, IL",
        "UIC the #KefirTruck has landed! Come treat yourself between classes with a cup of frozen goodness. Flavors are pumpkin spice and original.");
  }

  @Test
  public void testUICRush() {
    assertTweet("600 South Paulina, Chicago, IL",
        "UIC Rush recevied requests to return we are back in 5 minutes :) !!");
  }

  public void testUICMedical() {
    assertTweet("Wood and Taylor, Chicago, IL",
        "UIC Medical District today at 11:30! Menu is posted at thesouthernmac.com fb.me/PnVGkbvf");

  }

  @Test @Ignore
  public void testSouthLoop() {
    assertTweet("13th and Michigan Avenue, Chicago, IL",
        "GiGisBakeShop: My apologies South Loop..got a spot and opened the window to a crowd of hungry CUPCAKE lovers!  Parked at 13th / S... http://t.co/JUE2oB8A");
  }

  @Test
  public void testIntersection10() {
    assertTweet("Damen and Schiller, Chicago, IL",
        "Arrived at south end of Wicker Park on Damen and Schiller.");
  }

  @Test
  public void testMixedCaseIntersection() {
    assertTweet("Wacker and LaSalle, Chicago, IL",
        "Was able to make it out to Wacker & LaSalle for a quick stop!! @CareerBuilder come on down... We'll be leaving just after 4pm.");
  }

  @Test
  public void testSpecialCaseBetween() {
    // I know I can't detect all special cases, but 57th/58th/Ellis seems to be specified a
    // whole bunch of different ways.  Just search for the combination of 57th, 58th and Ellis somewhere
    // in the string
    assertTweet("57th and Ellis, Chicago, IL",
        "hautesausage: We r on 57th in front of The Reg till 1:35pm.");
//    assertTweet("57th and Ellis, Chicago, IL",
//        "GiGisBakeShop: GiGi and the PURPLE Bus has moved to to Ellis (57th / 58th)..Come get a cupcake!");
//    assertTweet("57th and Ellis, Chicago, IL", "Sweetie cakes in Hyde park! 57is & ellis");
  }

  @Test
  public void testNEIU() {
    assertTweet("5500 North Saint Louis Avenue, Chicago, IL",
        "Hey guys we are right in front of NEIU! On Bryn Mawr you cant miss us!");
  }

  @Test
  public void testAON() {
    assertTweet("Randolph and Columbus, Chicago, IL",
        "The Tamale Guy is @ #aon center today!! hot, handmade, fresh #tamales.. pork,chicken and veggie!! #chicago #foodtrucks #fb");
  }

  @Test
  public void testIntersectionBlvd() {
    assertTweet("Jackson Blvd and Wacker Drive, Chicago, IL",
        "fidotogo: We are at Jackson Blvd and Wacker Drive! It's beautiful out! Come on out! :)");
    assertTweet("North Ave and Sheffield, Chicago, IL",
        "Landed at North Ave and Sheffield!  Come get your CUPCAKES!!");
    assertTweet("Michigan and Walton, Chicago, IL",
        "Arrived at Michigan and Walton. Come get your Sunday macaron going!");
  }

  @Test
  public void testMultiwordStreetName() {
    assertTweet("Wacker and Van Buren, Chicago, IL",
        "HomageSF: Landed at Wacker and Van Buren .  Old spot is closed for good. Esquites is on the menu per a special request.");
  }

  @Test
  public void testMerchandiseMart() {
    assertTweet("Merchandise Mart",
        "brownbagtruck: En route Merch Mart! Might be running a skoach late! Post upon landing! 600 W, shooting for 12:30 still!");
  }

  @Test @Ignore(
      "Want to handle these lower case intersections.  Should match preposition before the intersection?")
  public void testSpecialCaseIntersection() {
    assertTweet("Clinton and Lake, Chicago, IL",
        "SweetiecakesC: Yeah the sun is out! Come down to Clinton/lake for some cupcakes w tamales,,,, yum yum");
    assertTweet("Rush and Oak, Chicago, IL",
        "We got parking on rush and oak! Come celebrate with us - we're fully stocked!");
    assertTweet("Clinton and Lake, Chicago, IL",
        "CourageousCakes: Guess what chicken butt....cruising to Adams/clinton early!!");
  }

  @Test
  public void testOldTown() {
    assertTweet("North and Wells, Chicago, IL", "#KefirTruck on the move! Next stop Old Town!!");
  }

  @Test @Ignore("Not ready for prime time")
  public void testIntersectionWithMixedCase() {
    assertTweet("North Avenue and Cannon Drive, Chicago, IL",
        "Hey we just arrived at North Fields on North ave and Cannon dr. We are in the zoo parking lot!");
    assertTweet("Wacker and Van Buren, Chicago, IL", "adelitatruck: Wacker and van buren today");
  }

  @Test
  public void testSpecialIntersection() {
    assertTweet("N Milwaukee Ave & W Grand Ave & N Halsted St, Chicago, IL",
        "#latenighttamales @grand halsted milwaukee.. till 3:30AM.. next to @ORANGE_CHICAGO & @BuddhaLounge #foodtrucks #chicago #fb #tamales");
  }

  private void assertTweet(String expected, String tweetText, Truck truck) {
    List<String> addresses = parser.parse(tweetText, truck);
    assertNotNull(addresses);
    assertEquals(1, addresses.size());
    assertEquals(expected, addresses.get(0));
  }

  private void assertTweet(String expected, String tweet) {
    assertTweet(expected, tweet, truck);
  }
}
