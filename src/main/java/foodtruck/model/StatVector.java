package foodtruck.model;

import java.util.List;

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
}
