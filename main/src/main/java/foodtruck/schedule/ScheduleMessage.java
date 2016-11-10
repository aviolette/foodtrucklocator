package foodtruck.schedule;

import java.util.List;

import com.google.common.collect.ImmutableList;

import org.joda.time.format.DateTimeFormatter;

import foodtruck.model.TruckSchedule;
import foodtruck.model.TruckStop;

/**
 * @author aviolette
 * @since 11/9/16
 */
public class ScheduleMessage {
  private static final int MAX_TWITTER_LENGTH = 140;
  private String fullSchedule;

  ScheduleMessage(String message) {
    this.fullSchedule = message;
  }

  public static Builder builder() {
    return new Builder();
  }

  public List<String> getTwitterMessages() {
    String fullSchedule = getFullSchedule();
    ImmutableList.Builder<String> builder = ImmutableList.builder();

    String remainder = fullSchedule;
    while (true) {
      if (remainder.length() < MAX_TWITTER_LENGTH) {
        if (remainder.length() > 0) {
          builder.add(remainder);
        }
        break;
      } else {
        // TODO: make this broken across lines
        int index;
        StringBuilder subBuilder = new StringBuilder();
        while (true) {
          index = remainder.indexOf('\n');
          if (index >= MAX_TWITTER_LENGTH || index == -1) {
            int remaining = MAX_TWITTER_LENGTH - subBuilder.length();
            String portion = remainder.substring(0, remaining);
            builder.add(subBuilder.append(portion)
                .toString());
            remainder = remainder.substring(remaining);
            break;
          }
          String portion = remainder.substring(0, index + 1);
          if (portion.length() + subBuilder.length() > MAX_TWITTER_LENGTH) {
            builder.add(subBuilder.toString());
            break;
          }
          subBuilder.append(portion);
          remainder = remainder.substring(index + 1);
        }
      }
    }
    return builder.build();
  }

  public String getFullSchedule() {
    return fullSchedule;
  }

  public String getFullScheduleAsHtml() {
    return fullSchedule.replaceAll("\n", "<br/>");
  }

  public static class Builder {
    private DateTimeFormatter formatter;
    private TruckSchedule schedule;

    public Builder() {
    }

    public ScheduleMessage build() {
      StringBuilder message = new StringBuilder("Today's Schedule:\n");
      for (TruckStop stop : schedule.getStops()) {
        message.append(formatter.print(stop.getStartTime()))
            .append(" ")
            .append(stop.getLocation()
                .getShortenedName())
            .append("\n");
      }
      return new ScheduleMessage(message.toString());
    }

    public Builder formatter(DateTimeFormatter formatter) {
      this.formatter = formatter;
      return this;
    }

    public Builder schedule(TruckSchedule schedule) {
      this.schedule = schedule;
      return this;
    }
  }
}
