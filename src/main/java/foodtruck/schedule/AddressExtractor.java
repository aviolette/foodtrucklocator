package foodtruck.schedule;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * @author aviolette@gmail.com
 * @since Jul 20, 2011
 */
public class AddressExtractor {
  private final List<Pattern> patterns;
  private final String defaultCity;

  public AddressExtractor() {
    patterns = ImmutableList.of(
       Pattern.compile("[a-zA-Z]+((\\s+(and)\\s+)|(\\s*(\\&|\\\\|\\/)\\s*))[a-zA-Z]+"),
        Pattern.compile("(^:)*\\d+\\s+[NnSsEeWw]\\.*\\s+\\w+")
    );
    defaultCity = "Chicago, IL";
  }

  List<String> parse(String tweet) {
    ImmutableList.Builder<String> addresses = ImmutableList.builder();
    for ( Pattern p : patterns) {
      Matcher m = p.matcher(tweet);
      while (m.find()) {
        addresses.add(tweet.substring(m.start(), m.end()) + ", " + defaultCity);
      }
    }
    return addresses.build();
  }

  public String parseFirst(String tweetText) {
    return Iterables.getFirst(parse(tweetText), null);
  }
}
