package foodtruck.notifications;

import java.util.List;
import java.util.Objects;

import com.google.common.collect.ImmutableList;

import foodtruck.socialmedia.MessageSplitter;

/**
 * @author aviolette
 * @since 11/15/17
 */
public class TruckStopSplitter implements MessageSplitter {
  private final String name;

  public TruckStopSplitter(String name) {
    this.name = name;
  }

  @Override
  public List<String> split(String status) {
    ImmutableList.Builder<String> statuses = ImmutableList.builder();
    int start = 0, end = 140, cutoff = 140;
    // TODO: This doesn't work when greater than 280
    while (true) {
      int min = Math.min(status.substring(start).length(), end);
      String chunk = status.substring(start, start + min);
      if (start != 0) {
        chunk = "Additional trucks at " + name + ":" + chunk;
      }
      if (min < cutoff) {
        statuses.add(chunk);
        break;
      } else {
        end = status.substring(0, end).lastIndexOf(' ');
        statuses.add(status.substring(start, end));
        start = end;
        end = end + 140;
      }
    }
    return statuses.build().reverse();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(name);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else if (!(obj instanceof TruckStopSplitter)) {
      return false;
    }
    return Objects.equals(name, ((TruckStopSplitter)obj).name);
  }
}
