package foodtruck.monitoring;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * @author aviolette
 * @since 10/14/18
 */
public class StatUpdate {

  private final String name;
  private final int amount;
  private final long timestamp;

  @JsonCreator
  public StatUpdate(@JsonProperty("name") String name, @JsonProperty("amount") int amount, @JsonProperty("timestamp") long timestamp) {
    this.name = name;
    this.amount = amount;
    this.timestamp = timestamp;
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

  @Override
  public String toString() {
    return "StatUpdate{" + "name='" + name + '\'' + ", amount=" + amount + ", timestamp=" + timestamp + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    StatUpdate that = (StatUpdate) o;
    return amount == that.amount && Objects.equals(name, that.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, amount);
  }
}
