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
  public void testFormat_parseUrl() {
    assertEquals("Thank you chi for a bright &amp; sweet day! Rolling tomorrow with more of your favorites...STOPS: 11AM Madison/Wacker... <a target=\"_blank\" href=\"http://t.co/XrcyI1ri\">http://t.co/XrcyI1ri</a>",
        formatBody("Thank you chi for a bright & sweet day! Rolling tomorrow with more of your favorites...STOPS: 11AM Madison/Wacker... http://t.co/XrcyI1ri"));
  }

  @Test
  public void testFormat_sanitizeHtml() {
    assertEquals("Going strong Ship @&lt;&lt;Clark &amp; Washington&gt;&gt; Shuttle heading to &lt;&lt;600 W Chicago&gt;&gt; see you soon!!", formatBody("Going strong Ship @<<Clark & Washington>> Shuttle heading to <<600 W Chicago>> see you soon!!"));
  }

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
