package foodtruck.schedule;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import foodtruck.model.Truck;

/**
 * @author aviolette@gmail.com
 * @since Jul 20, 2011
 */
public class AddressExtractor {
  private final List<PatternTransform> patterns;
  private final static String INTERSECTION_PARTIAL =
      "(N|E|W|S\\s+)?[A-Z0-9][a-zA-Z0-9]+(\\s+(st|St|Drive|Dr|Buren|BUREN|Blvd|Ave)\\.?)?";
  private final static String INTERSECTION_AND =
      "((\\s+(and|near|n)\\s+)|(\\s*(\\&|\\\\|\\/)\\s*))";
  private final static Pattern NUMBER_AND_NUMBER =
      Pattern.compile("(\\d+) and (\\d+), Chicago, IL");

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
        return
            input.replaceAll("(&|b\\/w|\\/)", " and ").replace(" at ", " and ").replace("  and  ",
                " and ").replace(" between ", " and ").replace(" in", "").replace(" n ", " and ")
                .replace(" on ", " and ").replace(" near ", " and ")
                .trim() +
                ", Chicago, IL";
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
        // University of Chicago
        new PatternTransform(
            Pattern.compile("U of Chicago|UofC|U of C|UChicago|Ellis \\(57 / 58\\)|The Reg\\b",
                Pattern.CASE_INSENSITIVE),
            keyword("57th and Ellis, Chicago, IL"), true, 0),
        new PatternTransform(Pattern.compile("\\bjeff/jack\\b", Pattern.CASE_INSENSITIVE),
            keyword("Jefferson and Jackson, Chicago, IL"), false, 0),
        // Harpo Studios
        new PatternTransform(
            Pattern.compile("(harpo|rosie)\\b", Pattern.CASE_INSENSITIVE), keyword("Harpo Studios"),
            true, 0),
        // Brown line stops
        new PatternTransform(Pattern.compile("(Paulina|Sedgwick) Brown Line",
            Pattern.CASE_INSENSITIVE), null, true, 0),
        new PatternTransform(Pattern.compile("Trump", Pattern.CASE_INSENSITIVE),
            keyword("400 North Wabash, Chicago, IL"), true, 0),
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
        // address format
        new PatternTransform(Pattern.compile("(^:)*\\d+\\s*[NnSsEeWw]\\.?\\s+\\w+"), cityAppender,
            true, 0),
        // Willis Tower
        new PatternTransform(Pattern.compile("sears|willis", Pattern.CASE_INSENSITIVE),
            keyword("Wacker and Van Buren, Chicago, IL"), true, 0),
        // Rush Medical
        new PatternTransform(Pattern
            .compile("\\buic\\b(.*)\\brush\\b|\\brush\\b(.*)\\buic\\b|\\brush medical\\b",
                Pattern.CASE_INSENSITIVE),
            keyword("600 South Paulina, Chicago, IL"), true, 0),
        // UIC Medical
        new PatternTransform(Pattern
            .compile("\\buic medical\\b", Pattern.CASE_INSENSITIVE),
            keyword("Wood and Taylor, Chicago, IL"), true, 0),
/*
        // UIC
        new PatternTransform(Pattern.compile("UIC\\b", Pattern.CASE_INSENSITIVE),
            keyword("Vernon Park Circle, Chicago, IL"), true, 0),
            */
        // tamale spaceship format
        new PatternTransform(Pattern.compile("<<(.*)>>"), null, true, 1),
        // Between two streets
        new PatternTransform(Pattern.compile(
            "between " + INTERSECTION_PARTIAL + INTERSECTION_AND + "(" +
                INTERSECTION_PARTIAL + " on " + INTERSECTION_PARTIAL + ")"),
            cityAppender, true, 9),
        // Between two streets
        new PatternTransform(Pattern.compile(
            "(" + INTERSECTION_PARTIAL + " ((in )?between|b/w) " + INTERSECTION_PARTIAL +
                ")((\\s+(and)\\s+)|(\\s*(\\&|\\\\|\\/)\\s*))" + INTERSECTION_PARTIAL),
            cityAppender, true, 1),
        // Between two streets
        new PatternTransform(Pattern.compile(
            "(" + INTERSECTION_PARTIAL + " ((in )?between|b/w) " + INTERSECTION_PARTIAL +
                ")((\\s+(and)\\s+)|(\\s*(\\&|\\\\|\\/)\\s*))" + INTERSECTION_PARTIAL,
            Pattern.CASE_INSENSITIVE),
            cityAppender, true, 1, ImmutableSet.of("steakwch")),
        // intersection format preceded by at
        new PatternTransform(Pattern.compile(
            "[A-Z0-9][a-zA-Z0-9]+\\s+at\\s+(" + INTERSECTION_PARTIAL +
                "((\\s+(and|n)\\s+)|(\\s*(\\&|\\\\|\\/)\\s*))" + INTERSECTION_PARTIAL + ")"),
            cityAppender, true, 1),
        // intersection format
        new PatternTransform(Pattern.compile(
            INTERSECTION_PARTIAL + INTERSECTION_AND + INTERSECTION_PARTIAL),
            cityAppender, false, 0),
        // case-insensitive intersection
        new PatternTransform(Pattern.compile(
            "at (" + INTERSECTION_PARTIAL + INTERSECTION_AND + INTERSECTION_PARTIAL + ")",
            Pattern.CASE_INSENSITIVE),
            cityAppender, false, 1,
            ImmutableSet
                .of("steakwch", "rzjp6cakes", "flirtycupcakes", "theslideride", "caponiesexp",
                    "pecanandcharlie")),
        // special case intersections
        // Merchandise Mart
        new PatternTransform(
            Pattern.compile("Merch(andise)? mart|#MerchandiseMart", Pattern.CASE_INSENSITIVE),
            keyword("Merchandise Mart"), true, 0),
        new PatternTransform(Pattern.compile("wttw", Pattern.CASE_INSENSITIVE),
            keyword("WTTW"), true, 0),
        // tribune tower
        new PatternTransform(Pattern.compile("tribune tower", Pattern.CASE_INSENSITIVE),
            keyword("Tribune Tower, Chicago, IL"), true, 0),
        // keyword format
        new PatternTransform(
            Pattern.compile("grant park|presidential towers", Pattern.CASE_INSENSITIVE),
            keywordReplace, false, 0)
    );
  }

  List<String> parse(String tweet, Truck truck) {
    ImmutableList.Builder<String> addresses = ImmutableList.builder();
    applyTruckSpecificMatches(tweet, truck, addresses);
    String truckId = truck.getId();
    for (PatternTransform p : patterns) {
      if (!p.findAndMatch(tweet, addresses, truckId)) {
        break;
      }
    }
    return addresses.build();
  }

  private void applyTruckSpecificMatches(String tweet, Truck truck,
      ImmutableList.Builder<String> addresses) {
    // TODO: implement this as a set of pattern matchers on a truck
    final String lowerCaseTweet = tweet.toLowerCase();
    if ("mjexpress14".equals(truck.getId())) {
      if (lowerCaseTweet.contains("library")) {
        addresses.add("834 Lake St, Oak Park, IL");
        return;
      } else if (lowerCaseTweet.contains("theater")) {
        addresses.add("1010 Lake St, Oak Park, IL");
      }
    }

    String tmpTweet = tweet.toLowerCase();
    if (tmpTweet.contains("grand") && tmpTweet.contains("halsted") &&
        tmpTweet.contains("milwaukee")) {
      addresses.add("N Milwaukee Ave & W Grand Ave & N Halsted St, Chicago, IL");
    }
  }

  public String parseFirst(String tweetText, Truck truck) {
    return Iterables.getFirst(parse(tweetText, truck), null);
  }

  private static boolean inNegativeList(String address, String truckId) {
    if ("fidotogo".equals(truckId)) {
      return address.equals("5212 N. Clark, Chicago, IL") ||
          address.equals("729 W Randolph, Chicago, IL");
    } else if ("thesouthernmac".equals(truckId)) {
      return address.equals("60 E. Lake, Chicago, IL");
    } else if ("gaztrowagon".equals(truckId)) {
      return address.toLowerCase().equals("5973 n clark, chicago, il");
    } else if ("keifertruck".equals(truckId)) {
      return address.equals("Merchandise Mart");
    }
    // TODO: should probably adjust my regex's but its a little easier to do here.
    return NUMBER_AND_NUMBER.matcher(address).find();
  }

  private static class PatternTransform {
    private final Pattern pattern;
    private final @Nullable Function<String, String> transformer;
    private final boolean breakHere;
    private int group;
    private Set<String> restrictionSet;

    public PatternTransform(Pattern pattern, @Nullable Function<String, String> transformer,
        boolean breakHere, int group) {
      this(pattern, transformer, breakHere, group, ImmutableSet.<String>of());
    }

    public PatternTransform(Pattern pattern, @Nullable Function<String, String> transformer,
        boolean breakHere, int group, Set<String> restrictionSet) {
      this.pattern = pattern;
      this.transformer = transformer;
      this.breakHere = breakHere;
      this.group = group;
      this.restrictionSet = restrictionSet;
    }

    public boolean findAndMatch(String tweet, ImmutableList.Builder<String> addresses,
        String truckId) {
      if (!restrictionSet.isEmpty() && !restrictionSet.contains(truckId)) {
        return true;
      }
      Matcher m = pattern.matcher(tweet);
      while (m.find()) {
        String matched = m.group(group);
        final String address = transformer == null ? matched : transformer.apply(matched);
        if (inNegativeList(address, truckId)) {
          continue;
        }
        addresses.add(address);
        if (breakHere) return false;
      }
      return true;
    }
  }
}
