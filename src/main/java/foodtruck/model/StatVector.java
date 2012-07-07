// Copyright 2012 BrightTag, Inc. All rights reserved.
package foodtruck.model;

import java.util.List;

/**
 * @author aviolette@gmail.com
 * @since 7/6/12
 */
public class StatVector {
  private String name;
  private String color;
  private List<TimeValue> dataPoints;

  public StatVector(String name, String color, List<TimeValue> dataPoints) {
    this.name = name;
    this.color = color;
    this.dataPoints = dataPoints;
  }

  public String getName() {
    return name;
  }

  public String getColor() {
    return color;
  }

  public List<TimeValue> getDataPoints() {
    return dataPoints;
  }

}
