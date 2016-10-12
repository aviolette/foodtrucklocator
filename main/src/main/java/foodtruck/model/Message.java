package foodtruck.model;

import org.joda.time.DateTime;

/**
 * @author aviolette
 * @since 2/6/14
 */
public class Message extends ModelEntity {
  private final String message;
  private final DateTime startTime;
  private final DateTime endTime;

  public Message(Long key, String message, DateTime startTime, DateTime endTime) {
    super(key);
    this.message = message;
    this.startTime = startTime;
    this.endTime = endTime;
  }

  public String getMessage() {
    return message;
  }

  public DateTime getStartTime() {
    return startTime;
  }

  public DateTime getEndTime() {
    return endTime;
  }

  @Override public String toString() {
    return message;
  }
}
