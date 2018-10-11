package foodtruck.appengine.monitoring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.api.Metric;
import com.google.api.MonitoredResource;
import com.google.appengine.api.utils.SystemProperty;
import com.google.cloud.monitoring.v3.MetricServiceClient;
import com.google.inject.Inject;
import com.google.monitoring.v3.CreateTimeSeriesRequest;
import com.google.monitoring.v3.Point;
import com.google.monitoring.v3.ProjectName;
import com.google.monitoring.v3.TimeInterval;
import com.google.monitoring.v3.TimeSeries;
import com.google.monitoring.v3.TypedValue;
import com.google.protobuf.util.Timestamps;

import foodtruck.monitoring.CounterPublisher;
import foodtruck.time.Clock;

/**
 * @author aviolette
 * @since 10/8/18
 */
public class StackDriverPublisher implements CounterPublisher {

  private static final Logger log = Logger.getLogger(StackDriverPublisher.class.getName());

  private final Clock clock;

  @Inject
  public StackDriverPublisher(Clock clock) {
    this.clock = clock;
  }

  @Override
  public void increment(String name) {
    increment(name, 1);
  }

  @Override
  public void increment(String propertyName, int amount) {
    log.info("Writing out property: " + propertyName);
    ;
    try (MetricServiceClient client = MetricServiceClient.create()) {


      TimeInterval interval = TimeInterval.newBuilder()
          .setEndTime(Timestamps.fromMillis(clock.nowInMillis()))
          .build();

      TypedValue value = TypedValue.newBuilder()
          .setInt64Value(amount)
          .build();

      Point point = Point.newBuilder()
          .setInterval(interval)
          .setValue(value)
          .build();

      List<Point> pointList = new ArrayList<>();
      pointList.add(point);

      ProjectName name = ProjectName.of(SystemProperty.applicationId.get());

      Map<String, String> metricLabels = new HashMap<>();
      Metric metric = Metric.newBuilder()
          .setType("custom.googleapis.com/" + propertyName)
          .putAllLabels(metricLabels)
          .build();

      Map<String, String> resourceLabels = new HashMap<>();

      MonitoredResource resource = MonitoredResource.newBuilder()
          .setType("global")
          .putAllLabels(resourceLabels)
          .build();

      TimeSeries timeSeries = TimeSeries.newBuilder()
          .setMetric(metric)
          .setResource(resource)
          .addAllPoints(pointList)
          .build();

      List<TimeSeries> timeSeriesList = new ArrayList<>();
      timeSeriesList.add(timeSeries);

      CreateTimeSeriesRequest request = CreateTimeSeriesRequest.newBuilder()
          .setName(name.toString())
          .addAllTimeSeries(timeSeriesList)
          .build();

      client.createTimeSeries(request);
    } catch (IOException e) {
      log.log(Level.INFO, e.getMessage(), e);
    }
  }
}
