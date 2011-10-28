package foodtruck.schedule;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;

import foodtruck.model.TweetSummary;
import static org.junit.Assert.assertEquals;

/**
 * @author aviolette@gmail.com
 * @since 10/20/11
 */
public class TerminationDetectorTest {
  private DateTime tweetTime;
  private TerminationDetector detector;
  private TweetSummary.Builder tweetBuilder;

  @Before
  public void before() {
    tweetTime = new DateTime(2011, 10, 10, 9, 8, 7, 0, DateTimeZone.UTC);
    detector = new TerminationDetector();
    tweetBuilder = new TweetSummary.Builder().time(tweetTime);
  }

  @Test
  public void testSellOut() {
    assertEquals(tweetTime,
        detector.detect(tweetBuilder.text("Sold out! thanks 600 w. Chicago").build()));
  }

  @Test
  public void testHeadingBack() {
    assertEquals(tweetTime, detector.detect(tweetBuilder.text(
        "Heading back to our store in Lincoln Park, come visit us to try our delicious and unique meatloaf!")
        .build()));
  }

  @Test
  public void testPartialSellOut() {
    assertEquals(tweetTime.plusMinutes(15), detector.detect(tweetBuilder.text(
        "SweetSpotMac: Almost sold out!still got pistachio, strawberry and our special Reese's treat! come get it while it last!Were in front of the tribune tower!")
        .build()));
    assertTweet(null, "SOLD OUT of Meatloaf Muffins!");
  }

  @Test
  public void testThankYou() {
    assertEquals(tweetTime, detector.detect(tweetBuilder.text(
        "Thank you U of Chicago for braving the weather today and South Loop for closing out our day!  Enjoy the rest of your night!")
        .build()));
    assertEquals(tweetTime, detector.detect(tweetBuilder.text(
        "fidotogo: Thank you so much everyone in Andersonville! Goodnight Stockton, Prince, Toby, Shaggy, Anthony  and all the rest of our wonderful pups!...")
        .build()));
    assertTweet(tweetTime, "MamaGreenGoodie: Thanks for a great day everyone!! See you tomorrow.");
  }

  private void assertTweet(DateTime time, String tweetText) {
    assertEquals(time, detector.detect(tweetBuilder.text(tweetText).build()));
  }

  @Test
  public void test2() {
    assertEquals(tweetTime,
        detector.detect(tweetBuilder.text("Thanks so very much AON&Streeterville!!").build()));
  }

  @Test
  public void testGoodNight() {
    assertEquals(tweetTime, detector.detect(tweetBuilder.text("Good night Chicago!").build()));
  }

  @Test
  public void testAllSoldOut() {
    assertEquals(tweetTime, detector.detect(
        tweetBuilder.text("LQMeatMobile: All sold out for the day! Thanks everyone!").build()));
  }

  @Test
  public void testLastCall_PutItAtFifteenMinutesAhead() {
    assertEquals(tweetTime.plusMinutes(15),
        detector.detect(
            tweetBuilder.text("Culture: Last Call River North!!  If you are on your way, tweet us")
                .build()));
  }
}
