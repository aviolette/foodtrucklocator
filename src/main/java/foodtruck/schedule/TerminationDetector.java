package foodtruck.schedule;

import com.google.inject.Inject;

import org.joda.time.DateTime;

import foodtruck.model.TweetSummary;
import foodtruck.util.Clock;

/**
 * Detects termination messages in tweets
 * @author aviolette@gmail.com
 * @since 10/20/11
 */
public class TerminationDetector {
  private final Clock clock;

  @Inject
  public TerminationDetector(Clock clock) {
    this.clock = clock;
  }

  // TODO: probably need an abstraction like TruckStopMatch to handle terminations
  public DateTime detect(TweetSummary tweet) {
    String tweetText = tweet.getText().toLowerCase();
    if (tweetText.contains("sold out") || tweetText.contains("good-bye") ||
        tweetText.contains("good night") || tweetText.contains("good bye")
        || tweetText.contains("leaving") || tweetText.contains("heading out")
        || tweetText.contains("thanks") || tweetText.contains("thank you")) {
      return clock.now();
    }
    return null;
  }
}
