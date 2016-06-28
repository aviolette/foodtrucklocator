package foodtruck.socialmedia;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import foodtruck.dao.DailyDataDAO;
import foodtruck.model.DailyData;
import foodtruck.model.Story;
import foodtruck.model.Truck;
import foodtruck.util.Clock;

/**
 * This is kind of weird, it extracts day-specific information (such as specials) from social media accounts associated
 * with trucks.
 * @author aviolette
 * @since 10/28/15
 */
public class SpecialUpdater {
  private static final Logger log = Logger.getLogger(SpecialUpdater.class.getName());
  private final DailyDataDAO dailyDataDAO;
  private final Pattern oldFashionedPattern = Pattern.compile("(\\w+)\\s+(\\w+) old fashioned(.*)");
  private final Pattern cakeMatcher = Pattern.compile("(\\w+)\\s+(\\w+) cake");
  private final Pattern jellyPattern = Pattern.compile("filled is (\\w+)");
  private final Clock clock;

  @Inject
  public SpecialUpdater(DailyDataDAO dailyDataDAO, Clock clock) {
    this.dailyDataDAO = dailyDataDAO;
    this.clock = clock;
  }

  public void update(Truck truck, List<Story> stories) {
    // TODO: need to genercize this
    if (!"thevaultvan".equals(truck.getId())) {
      return;
    }
    log.log(Level.INFO, "Checking this out {0}", truck.getId());
    for (Story story : Lists.reverse(stories)) {
      String lower = story.getText().toLowerCase();
      String locationName = null, truckId = null;
      if (lower.contains("#canalvault")) {
        locationName = "Doughnut Vault @ Canal";
      } else if (lower.contains("#franklinvault")) {
        locationName = "Doughnut Vault @ Franklin";
      } else if (lower.contains("#vaultvan")) {
        truckId = "thevaultvan";
      }
      if (locationName != null || truckId != null) {
        if (lower.contains("sold out")) {
          log.log(Level.INFO, locationName + " sold out");
          DailyData dailyData = findDailyData(locationName, truckId);
          if (dailyData != null) {
            dailyDataDAO.save(dailyData.markAllSoldOut());
          }
        } else  {
          DailyData dailyData = findDailyData(locationName, truckId);
          DailyData.Builder specialsBuilder;
          if (dailyData == null) {
            specialsBuilder = DailyData.builder()
                .onDate(clock.currentDay())
                .clearSpecials()
                .truckId(truckId)
                .locationId(locationName);
          } else {
            specialsBuilder = DailyData.builder(dailyData);
          }
          boolean changed = detectOldFashioned(lower, locationName, specialsBuilder, truckId);
          changed = detectCake(lower, locationName, specialsBuilder, truckId) || changed;
          changed = detectJelly(lower, specialsBuilder) || changed;
          changed = detectStack(lower, locationName, truckId, specialsBuilder) || changed;
          changed = detectPina(lower, locationName, truckId, specialsBuilder) || changed;
          changed = detectRedVelvet(lower, locationName, truckId, specialsBuilder) || changed;
          if (changed) {
            DailyData built = specialsBuilder.build();
            log.log(Level.INFO, "Saving {0}", built);
            dailyDataDAO.save(built);
          }
        }
      }
    }
  }

  private boolean detectPina(String lower, String locationName, String truckId, DailyData.Builder specialsBuilder) {
    if (lower.contains("piña colada special") || lower.contains("pina colada special")) {
      DailyData dailyData = specialsBuilder.clearSpecials()
          .addSpecial("Pinã Colada Old-fashioned", false)
          .locationId(locationName)
          .truckId(truckId)
          .build();
      log.log(Level.INFO, "Found special at {0}", dailyData);
      return true;
    }
    return false;
  }

  private boolean detectStack(String lower, String locationName, String truckId, DailyData.Builder specialsBuilder) {
    DailyData dailyData;
    if (lower.contains("powdered sugar stack")) {
      dailyData = specialsBuilder.clearSpecials().addSpecial("Powdered Sugar Stack", false)
          .locationId(locationName)
          .truckId(truckId)
          .build();
      log.log(Level.INFO, "Found special at {0}", dailyData);
      return true;
    }
    return false;
  }

  private boolean detectRedVelvet(String lower, String locationName, String truckId, DailyData.Builder specialsBuilder) {
    DailyData dailyData;
    if (lower.contains("red velvet")) {
      dailyData = specialsBuilder.clearSpecials().addSpecial("Red Velvet Cake", false)
          .locationId(locationName)
          .truckId(truckId)
          .build();
      log.log(Level.INFO, "Found special at {0}", dailyData);
      return true;
    }
    return false;
  }

  private DailyData findDailyData(String locationName, String truckId) {
    DailyData dailyData;
    if (locationName != null) {
      dailyData = dailyDataDAO.findByLocationAndDay(locationName, clock.currentDay());
    } else {
      dailyData = dailyDataDAO.findByTruckAndDay(truckId, clock.currentDay());
    }
    return dailyData;
  }

  private boolean detectCake(String lower, String locationName, DailyData.Builder specialsBuilder, String truckId) {
    DailyData dailyData;
    Matcher matcher = cakeMatcher.matcher(lower);
    if (matcher.find()) {
      StringBuilder builder = new StringBuilder();
      String first = matcher.group(1);
      if (lower.contains("double chocolate")) {
        builder.append("double ");
      } else if (lower.contains("mocha coconut crunch cake")) {
        builder.append("mocha ");
      }
      if (!first.matches("the|is|our")) {
        builder.append(first).append(" ");
      }
      builder.append(matcher.group(2));
      builder.append(" cake");
      String name = builder.toString();
      if (name.toLowerCase().contains("n cream")) {
        name = "Cookies n' Cream Cake";
      }
      dailyData = specialsBuilder.clearSpecials().addSpecial(name, false)
          .locationId(locationName)
          .truckId(truckId)
          .build();
      log.log(Level.INFO, "Found special at {0}", dailyData);
      return true;
    }
    return false;
  }

  private boolean detectJelly(String lower, DailyData.Builder specialsBuilder) {
    Matcher matcher = jellyPattern.matcher(lower);
    if (matcher.find()) {
      specialsBuilder.addSpecial(matcher.group(1) + " jelly", false);
      return true;
    }
    return false;
  }

  private boolean detectOldFashioned(String lower, String locationName, DailyData.Builder specialsBuilder,
      String truckId) {
    DailyData dailyData;
    Matcher matcher = oldFashionedPattern.matcher(lower);
    if (matcher.find()) {
      StringBuilder builder = new StringBuilder();
      String first = matcher.group(1);
      if (!first.matches("the|is|our|has")) {
        if (first.equalsIgnoreCase("lime") && lower.contains("key lime")) {
          builder.append("key ");
        }
        builder.append(first).append(" ");
      }
      builder.append(matcher.group(2));
      builder.append(" old fashioned");
      String rest = matcher.group(3);
      if (!Strings.isNullOrEmpty(rest)) {
        rest = rest.toLowerCase();
        if (rest.contains("chocolate drizzle")) {
          builder.append(" with chocolate drizzle");
        } else if (rest.contains("strawberry drizzle")) {
          builder.append(" with strawberry drizzle");
        } else if (rest.contains("crumble")) {
          builder.append(" with crumble");
        }
      }
      String special = builder.toString();
      if (special.startsWith("your favorite")) {
        return false;
      }
      dailyData = specialsBuilder.clearSpecials().addSpecial(special, false)
          .locationId(locationName)
          .truckId(truckId)
          .build();
      log.log(Level.INFO, "Found special at {0}", dailyData);
      return true;
    }
    return false;
  }
}

