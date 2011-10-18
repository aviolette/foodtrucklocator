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
        return input.replace(" & ", " and ") + ", Chicago, IL";
      }
    };
    Function<String, String> keywordReplace = new Function<String, String>() {
      final ImmutableMap<String, String> keywords = ImmutableMap.of("@wttw", "WTTW", "harpo",
          "Harpo Studios", "grant park", "Grant Park", "aon", "Randolph and Columbus, Chicago, IL",
          "presidential towers", "Presidential Towers");
      public String apply(String input) {
        return keywords.get(input.toLowerCase());
      }
    };
    final Pattern others = Pattern.compile(" w/ \\d+ others");
    Function<String, String> foursquareMassage = new Function<String, String>() {
      @Override public String apply(String input) {
        Matcher m = others.matcher(input);
        if (m.find()) {
          return input.substring(0, m.start());
        }
        return input;
      }
    };
    patterns = ImmutableList.of(
        // foursquare format
        new PatternTransform(Pattern.compile("\\(@ (.*)\\)"), foursquareMassage, true, 1),
        // tamale spaceship format
        new PatternTransform(Pattern.compile("<<(.*)>>"), null, true, 1),
        // intersection format
        new PatternTransform(Pattern.compile("[a-zA-Z]+((\\s+(and)\\s+)|(\\s*(\\&|\\\\|\\/)\\s*))[a-zA-Z]+"), cityAppender, false, 0),
        // address format
        new PatternTransform(Pattern.compile("(^:)*\\d+\\s*[NnSsEeWw]\\.*\\s+\\w+"), cityAppender, false, 0),
        // keyword format
        new PatternTransform(Pattern.compile("@wttw|harpo|grant park|aon|presidential towers", Pattern.CASE_INSENSITIVE), keywordReplace, false, 0)
    );
  }

  List<String> parse(String tweet) {
    ImmutableList.Builder<String> addresses = ImmutableList.builder();
    for ( PatternTransform  p : patterns) {
      if (!p.findAndMatch(tweet, addresses)) {
        break;
      }
    }
    return addresses.build();
  }

  public String parseFirst(String tweetText) {
    return Iterables.getFirst(parse(tweetText), null);
  }

  private static class PatternTransform {
    private final Pattern pattern;
    private final @Nullable Function<String, String> transformer;
    private final boolean breakHere;
    private int group;

    public PatternTransform(Pattern pattern, @Nullable Function<String, String> transformer,
        boolean breakHere, int group) {
      this.pattern = pattern;
      this.transformer = transformer;
      this.breakHere = breakHere;
      this.group = group;
    }

    public boolean findAndMatch(String tweet, ImmutableList.Builder<String> addresses) {
      Matcher m = pattern.matcher(tweet);
      while (m.find()) {

        String matched = m.group(group);
        addresses.add(transformer == null ? matched : transformer.apply(matched));
        if (breakHere) return false;
      }
      return true;
    }
  }
}
