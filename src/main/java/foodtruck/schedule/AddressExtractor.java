package foodtruck.schedule;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.RegEx;

import com.google.common.collect.ImmutableList;

/**
 * @author aviolette@gmail.com
 * @since Jul 20, 2011
 */
public class AddressExtractor {
  private final List<Pattern> patterns;

  public AddressExtractor() {
    patterns = ImmutableList.of(
       Pattern.compile("[a-zA-Z]+\\s*(and|\\&|\\\\|\\/)\\s*[a-zA-Z]+"),
        Pattern.compile("(^:)*\\d+\\s+[NnSsEeWw]\\.*\\s+\\w+")
    );
  }

  List<String> parse(String tweet) {
    ImmutableList.Builder<String> addresses = ImmutableList.builder();
    for ( Pattern p : patterns) {
      Matcher m = p.matcher(tweet);
      while (m.find()) {
        addresses.add(tweet.substring(m.start(), m.end()));
      }
    }
    return addresses.build();
  }
}
