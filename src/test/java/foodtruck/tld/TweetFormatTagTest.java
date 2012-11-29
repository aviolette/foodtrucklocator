package foodtruck.tld;

import org.junit.Test;

import static foodtruck.tld.TweetFormatTag.formatBody;
import static org.junit.Assert.assertEquals;

/**
 * @author aviolette
 * @since 11/29/12
 */
public class TweetFormatTagTest {
  @Test
  public void testFormat_noTweets() {
    assertEquals("There are no tweets here.", formatBody("There are no tweets here."));
  }

  @Test
  public void testFormat_tweet() {
    assertEquals("Foo <a target=\"_blank\" href=\"http://twitter.com/bar\">@bar</a>", formatBody("Foo @bar"));
  }

  @Test
  public void testFormat_tweetWithPeriod() {
    assertEquals("Foo <a target=\"_blank\" href=\"http://twitter.com/bar\">@bar</a>. At <a target=\"_blank\" href=\"http://twitter.com/foo\">@foo</a>", formatBody("Foo @bar. At @foo"));
  }
}
