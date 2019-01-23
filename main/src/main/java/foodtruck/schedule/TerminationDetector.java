package foodtruck.schedule;

import org.joda.time.DateTime;

import foodtruck.model.Story;

/**
 * Detects termination messages in tweets
 * @author aviolette@gmail.com
 * @since 10/20/11
 */
class TerminationDetector {
  // TODO: probably need an abstraction like TruckStopMatch to handle terminations
  DateTime detect(Story tweet) {
    if (tweet.isManualRetweet() || tweet.isReply()) {
      return null;
    }
    String tweetText = tweet.getText().toLowerCase();
    // TODO: use regex
    if (tweetText.contains("last call") || tweetText.contains("almost sold out!") || tweetText.contains("lastcall")) {
      return tweet.getTime().plusMinutes(15);
    }

    if (tweetText.contains("till next week") || tweetText.contains("til next week")) {
      return tweet.getTime();
    }

    if (tweetText.contains("done for the day")) {
      return tweet.getTime();
    }

    if (tweetText.contains("sold out of") || tweetText.contains("sold outta")) {
      return null;
    }

    if (tweetText.contains("til sold out") || tweetText.contains("till sold out")) {
      return null;
    }

    if (tweetText.contains("billy is done")) {
      return tweet.getTime();
    }

    if (tweetText.contains("or sold out")) {
      return null;
    }

    if (tweetText.contains("cash")) {
      return null;
    }

    if (tweetText.contains("sold out") || tweetText.contains("good-bye") ||
        tweetText.contains("good night") || tweetText.contains("good bye")
        || tweetText.contains("leaving")
        || tweetText.contains("see you next week")
        || tweetText.contains("see u next week")
        || tweetText.contains("apologies")
        || tweetText.contains("truck is closed")
        || tweetText.contains("buzzing")
        || tweetText.contains("thx")
        || tweetText.contains("muchas gracias")
        || tweetText.contains("adios ")
        || tweetText.startsWith("adios")
        || tweetText.contains("that's a wrap")
        || tweetText.contains("thats a wrap")
        || tweetText.contains("see ya next week")
        || tweetText.contains("see ya later")
        || tweetText.contains("till next time") || tweetText.contains("til next time")
        || tweetText.contains("heading back to our") || tweetText.contains("is on the move")
        || tweetText.contains("are on the move")) {
      return tweet.getTime();
    }
    return null;
  }
}
