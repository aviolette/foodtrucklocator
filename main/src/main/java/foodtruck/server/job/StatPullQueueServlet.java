package foodtruck.server.job;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.Metric;
import com.google.api.MonitoredResource;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskHandle;
import com.google.appengine.api.utils.SystemProperty;
import com.google.cloud.monitoring.v3.MetricServiceClient;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.monitoring.v3.CreateTimeSeriesRequest;
import com.google.monitoring.v3.Point;
import com.google.monitoring.v3.ProjectName;
import com.google.monitoring.v3.TimeInterval;
import com.google.monitoring.v3.TimeSeries;
import com.google.monitoring.v3.TypedValue;
import com.google.protobuf.util.Timestamps;

import foodtruck.model.Environment;
import foodtruck.monitoring.StatUpdate;
import foodtruck.time.Clock;

/**
 * @author aviolette
 * @since 10/14/18
 */
@Singleton
public class StatPullQueueServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(StatPullQueueServlet.class.getName());

  private final ObjectMapper mapper;
  private final Clock clock;

  @Inject
  public StatPullQueueServlet(ObjectMapper mapper, Clock clock, Environment environment) {
    this.mapper = mapper;
    this.clock = clock;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    Queue q = QueueFactory.getQueue("stats-queue");
    List<TaskHandle> tasks = q.leaseTasks(3600, TimeUnit.SECONDS, 100);
    log.log(Level.INFO, "Stats in queue: {0}", tasks.size());
    if (tasks.isEmpty()) {
      return;
    }

    Map<String, Integer> counts = new HashMap<>();
    long timeStamp = 0;
    for (TaskHandle task : tasks) {
      try {
        StatUpdate update = mapper.readValue(task.getPayload(), StatUpdate.class);
        timeStamp = update.getTimestamp();
        counts.merge(update.getName(), update.getAmount(), Integer::sum);
      } catch (Exception ex) {
        log.log(Level.INFO, "Problem serializing {0}", task.getPayload());
        log.log(Level.SEVERE, ex.getMessage(), ex);
      }
      q.deleteTask(task);
    }

    // handle transition period
    if (timeStamp == 0) {
      timeStamp = clock.nowInMillis();
    }
    try (MetricServiceClient client = MetricServiceClient.create()) {
      TimeInterval interval = TimeInterval.newBuilder()
          .setEndTime(Timestamps.fromMillis(timeStamp))
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
