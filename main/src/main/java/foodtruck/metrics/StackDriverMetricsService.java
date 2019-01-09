package foodtruck.metrics;

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

import foodtruck.model.Environment;

/**
 * @author aviolette
 * @since 2019-01-09
 */
public class StackDriverMetricsService implements MetricsService {

  private static final Logger log = Logger.getLogger(StackDriverMetricsService.class.getName());
  private final Environment environment;

  @Inject
  public StackDriverMetricsService(Environment environment) {
    this.environment = environment;
  }

  @Override
  public void updateStats(long timestamp, Map<String, Integer> counts) {

    if (environment == Environment.Development) {
      log.log(Level.INFO, "updating status: {0} {1}", new Object[] {timestamp, counts});
      return;
    }

    try (MetricServiceClient client = MetricServiceClient.create()) {
      TimeInterval interval = TimeInterval.newBuilder()
          .setEndTime(Timestamps.fromMillis(timestamp))
          .build();

      for (Map.Entry<String, Integer> count : counts.entrySet()) {

        TypedValue value = TypedValue.newBuilder()
            .setInt64Value(count.getValue())
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
            .setType("custom.googleapis.com/" + count.getKey())
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
        log.log(Level.INFO, "Sending {0} to stackdriver {1}", new Object[] {count.getKey(), count.getValue()});
        client.createTimeSeries(request);
      }
    } catch (IOException e) {
      log.log(Level.INFO, e.getMessage(), e);
    }

  }
}
