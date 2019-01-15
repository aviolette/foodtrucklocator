package foodtruck.monitoring;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

/**
 * @author aviolette
 * @since 10/14/18
 */
public class StatUpdate {

  private final String name;
  private final int amount;
  private final long timestamp;
  private final Map<String, String> labels;

  @JsonCreator
  public StatUpdate(@JsonProperty("name") String name, @JsonProperty("amount") int amount,
      @JsonProperty("timestamp") long timestamp, @JsonProperty("labels") Map<String, String> labels) {
    this.name = name;
    this.amount = amount;
    this.timestamp = timestamp;
    this.labels = labels;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public String getName() {
    return name;
  }

  public int getAmount() {
    return amount;
  }

  public Map<String, String> getLabels() {
    return labels;
  }

  @Override
  public String toString() {
    return "StatUpdate{" + "name='" + name + '\'' + ", amount=" + amount + ", timestamp=" + timestamp + ", labels=" +
        labels + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    StatUpdate that = (StatUpdate) o;
    return Objects.equal(name, that.name) && Objects.equal(labels, that.labels);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(name, labels);
  }
}
