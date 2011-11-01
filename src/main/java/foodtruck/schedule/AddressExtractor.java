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

  private static Function<String, String> keyword(final String toWhat) {
    return new Function<String, String>() {
      @Override public String apply(@Nullable String input) {
        return toWhat;
      }
    };
  }

  public AddressExtractor() {
    Function<String, String> cityAppender = new Function<String, String>() {
      public String apply(String input) {
        return input.replaceAll("(&|\\/)", " and ").replace(" at ", " and ").replace("  and  ",
            " and ").replace(" between ", " and ").replace(" n ", " and ").trim() + ", Chicago, IL";
      }
    };
    Function<String, String> keywordReplace = new Function<String, String>() {
      final ImmutableMap<String, String> keywords = ImmutableMap.of("@wttw", "WTTW", "harpo",
          "Harpo Studios", "grant park", "Grant Park",
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
        // University of Chicago
        new PatternTransform(
            Pattern.compile("U of Chicago|UofC|UChicago", Pattern.CASE_INSENSITIVE),
            keyword("57th and Ellis, Chicago, IL"), true, 0),
        // AON
        new PatternTransform(Pattern.compile("@aon| aon|#aon", Pattern.CASE_INSENSITIVE),
            keyword("Randolph and Columbus, Chicago, IL"), true, 0),
        // Old Town
        new PatternTransform(Pattern.compile("old town", Pattern.CASE_INSENSITIVE),
            keyword("North and Wells, Chicago, IL"), true, 0),
        // Lincoln Park Zoo
        new PatternTransform(Pattern.compile("lincoln park zoo", Pattern.CASE_INSENSITIVE),
            keyword("Lincoln Park Zoo, Chicago, IL"), true, 0),
        // Northeastern
        new PatternTransform(Pattern.compile("\\bNEIU\\b", Pattern.CASE_INSENSITIVE),
            keyword("5500 North Saint Louis Avenue, Chicago, IL"), true, 0),
        // Willis Tower
        new PatternTransform(Pattern.compile("sears|willis", Pattern.CASE_INSENSITIVE),
            keyword("Wacker and Adams, Chicago, IL"), true, 0),
        // UIC
        new PatternTransform(Pattern.compile("UIC", Pattern.CASE_INSENSITIVE),
            keyword("Vernon Park Circle, Chicago, IL"), true, 0),
        // tamale spaceship format
        new PatternTransform(Pattern.compile("<<(.*)>>"), null, true, 1),
        // Between two streets
        new PatternTransform(Pattern.compile(
            "([A-Z0-9][a-zA-Z0-9]+ between [A-Z0-9][a-zA-Z0-9]+)((\\s+(and|at)\\s+)|(\\s*(\\&|\\\\|\\/)\\s*))(N|E|W|S\\s+)?[A-Z0-9][a-zA-Z0-9]+"),
            cityAppender, true, 1),
        // intersection format preceded by at
        new PatternTransform(Pattern.compile(
            "[A-Z0-9][a-zA-Z0-9]+\\s+at\\s+((N|E|W|S\\s+)?[A-Z0-9][a-zA-Z0-9]+(\\s+(Blvd|Ave)\\.?)?((\\s+(and|at|n)\\s+)|(\\s*(\\&|\\\\|\\/)\\s*))(N|E|W|S\\s+)?[A-Z0-9][a-zA-Z0-9]+(\\s+(Blvd|Ave)\\.?)?)"),
            cityAppender, true, 1),
        // intersection format
        new PatternTransform(Pattern.compile(
            "(N|E|W|S\\s+)?[A-Z0-9][a-zA-Z0-9]+(\\s+(Blvd|Ave)\\.?)?((\\s+(and|at|n)\\s+)|(\\s*(\\&|\\\\|\\/)\\s*))(N|E|W|S\\s+)?[A-Z0-9][a-zA-Z0-9]+(\\s+(Blvd|Ave)\\.?)?"),
            cityAppender, false, 0),
        // special case intersections

        // address format
        new PatternTransform(Pattern.compile("(^:)*\\d+\\s*[NnSsEeWw]\\.*\\s+\\w+"), cityAppender,
            false, 0),
        // keyword format
        new PatternTransform(
            Pattern.compile("@wttw|harpo|grant park|presidential towers", Pattern.CASE_INSENSITIVE),
            keywordReplace, false, 0)
    );
  }

  List<String> parse(String tweet) {
    ImmutableList.Builder<String> addresses = ImmutableList.builder();
    for (PatternTransform p : patterns) {
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
