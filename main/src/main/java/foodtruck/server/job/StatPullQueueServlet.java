package foodtruck.server.job;

import java.io.IOException;
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
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskHandle;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.monitoring.StatUpdate;
import foodtruck.metrics.MetricsService;
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
  private final MetricsService service;

  @Inject
  public StatPullQueueServlet(ObjectMapper mapper, Clock clock, MetricsService service) {
    this.mapper = mapper;
    this.service = service;
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

    service.updateStats(timeStamp, counts);
  }
}
