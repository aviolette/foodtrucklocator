package foodtruck.schedule;

import org.joda.time.DateTime;

import foodtruck.model.TweetSummary;

/**
 * Detects termination messages in tweets
 * @author aviolette@gmail.com
 * @since 10/20/11
 */
public class TerminationDetector {
  // TODO: probably need an abstraction like TruckStopMatch to handle terminations
  public DateTime detect(TweetSummary tweet) {
    String tweetText = tweet.getText().toLowerCase();
    // TODO: use regex
    if (tweetText.contains("last call") || tweetText.contains("almost sold out!")) {
      return tweet.getTime().plusMinutes(15);
    }
    if (tweetText.contains("sold out of")) {
      return null;
    }
    if (tweetText.contains("sold out") || tweetText.contains("good-bye") ||
        tweetText.contains("good night") || tweetText.contains("good bye")
        || tweetText.contains("leaving")
        || tweetText.contains("heading back to our")
        || tweetText.contains("thanks") || tweetText.contains("thank you")) {
      return tweet.getTime();
    }
    return null;
  }
}
