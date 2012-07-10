package foodtruck.model;

import java.util.List;

import com.google.common.base.Objects;

/**
 * Represents a series of datapoints defined by a name
 * @author aviolette@gmail.com
 * @since 7/6/12
 */
public class StatVector {
  private String name;
  private List<TimeValue> dataPoints;

  public StatVector(String name, List<TimeValue> dataPoints) {
    this.name = name;
    this.dataPoints = dataPoints;
  }

  public String getName() {
    return name;
  }

  public List<TimeValue> getDataPoints() {
    return dataPoints;
  }

  @Override public String toString() {
    return Objects.toStringHelper(this)
        .add("name", name)
        .add("dataPoints", dataPoints).toString();
  }
}
