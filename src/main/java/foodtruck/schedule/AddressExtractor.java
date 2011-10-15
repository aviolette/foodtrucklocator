package foodtruck.schedule;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

/**
 * @author aviolette@gmail.com
 * @since Jul 20, 2011
 */
public class AddressExtractor {
  private final List<PatternTransform> patterns;

  public AddressExtractor() {
    Function<String, String> cityAppender = new Function<String, String>() {
      public String apply(String input) {
        return input + ", Chicago, IL";
      }
    };
    Function<String, String> keywordReplace = new Function<String, String>() {
      final ImmutableMap<String, String> keywords = ImmutableMap.of("@wttw", "WTTW", "harpo", "Harpo Studios", "grant park", "Grant Park");
      public String apply(String input) {
        return keywords.get(input.toLowerCase());
      }
    };
    patterns = ImmutableList.of(
        new PatternTransform(Pattern.compile("[a-zA-Z]+((\\s+(and)\\s+)|(\\s*(\\&|\\\\|\\/)\\s*))[a-zA-Z]+"), cityAppender),
        new PatternTransform(Pattern.compile("(^:)*\\d+\\s*[NnSsEeWw]\\.*\\s+\\w+"), cityAppender),
        new PatternTransform(Pattern.compile("@wttw|harpo|grant park", Pattern.CASE_INSENSITIVE), keywordReplace)
    );
  }

  List<String> parse(String tweet) {
    ImmutableList.Builder<String> addresses = ImmutableList.builder();
    for ( PatternTransform  p : patterns) {
      p.findAndMatch(tweet, addresses);
    }
    return addresses.build();
  }

  public String parseFirst(String tweetText) {
    return Iterables.getFirst(parse(tweetText), null);
  }

  private static class PatternTransform {
    private final Pattern pattern;
    private final @Nullable Function<String, String> transformer;

    public PatternTransform(Pattern pattern, @Nullable Function<String, String> transformer) {
      this.pattern = pattern;
      this.transformer = transformer;
    }

    public void findAndMatch(String tweet, ImmutableList.Builder<String> addresses) {
      Matcher m = pattern.matcher(tweet);
      while (m.find()) {
        String matched = tweet.substring(m.start(), m.end());
        addresses.add(transformer == null ? matched : transformer.apply(matched));
      }
    }
  }
}
