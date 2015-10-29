package foodtruck.socialmedia;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
  private final Pattern oldFashionedPattern = Pattern.compile("(\\w+)\\s+(\\w+) old fashioned");
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

    for (Story story : Lists.reverse(stories)) {
      String lower = story.getText().toLowerCase();
      String locationName = null;
      if (lower.contains("#canalvault")) {
        locationName = "Doughnut Vault @ Canal";
      } else if (lower.contains("#franklinvault")) {
        locationName = "Doughnut Vault @ Franklin";
      }
      if (locationName != null) {
        if (lower.contains("sold out")) {
          log.log(Level.INFO, locationName + " sold out");
          DailyData dailyData = dailyDataDAO.findByLocationAndDay(locationName, clock.currentDay());
          if (dailyData != null) {
            dailyDataDAO.save(dailyData.markAllSoldOut());
          }
        } else  {
          DailyData dailyData = dailyDataDAO.findByLocationAndDay(locationName, clock.currentDay());
          DailyData.Builder specialsBuilder;
          if (dailyData == null) {
            specialsBuilder = DailyData.builder()
                .onDate(clock.currentDay())
                .clearSpecials()
                .locationId(locationName);
          } else {
            specialsBuilder = DailyData.builder(dailyData);
          }
          boolean changed = detectOldFashioned(lower, locationName, specialsBuilder);
          changed = detectJelly(lower, locationName, specialsBuilder) || changed;
          if (changed) {
            DailyData built = specialsBuilder.build();
            log.log(Level.INFO, "Saving {0}", built);
            dailyDataDAO.save(built);
          }
        }
      }
    }
  }

  private boolean detectJelly(String lower, String locationName, DailyData.Builder specialsBuilder) {
    return false;
  }

  private boolean detectOldFashioned(String lower, String locationName, DailyData.Builder specialsBuilder) {
    DailyData dailyData;
    Matcher matcher = oldFashionedPattern.matcher(lower);
    if (matcher.find()) {
      StringBuilder builder = new StringBuilder();
      String first = matcher.group(1);
      if (!first.matches("the|is|our")) {
        builder.append(first).append(" ");
      }
      builder.append(matcher.group(2));
      builder.append(" old fashioned");
      dailyData = specialsBuilder.clearSpecials().addSpecial(builder.toString(), false)
          .locationId(locationName)
          .build();
      log.log(Level.INFO, "Found special at {0}", dailyData);
      return true;
    }
    return false;
  }
}

