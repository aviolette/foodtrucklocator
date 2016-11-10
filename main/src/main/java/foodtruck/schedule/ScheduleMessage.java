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
  private String fullSchedule;

  private ScheduleMessage(String message) {
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
      if (remainder.length() < 140) {
        if (remainder.length() > 0) {
          builder.add(remainder);
        }
        break;
      } else {
        // TODO: make this broken across lines
        String portion = remainder.substring(0, 140);
        builder.add(portion);
        remainder = remainder.substring(140);
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
