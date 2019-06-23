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
import com.google.appengine.api.taskqueue.TransientFailureException;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.metrics.MetricsService;
import foodtruck.monitoring.StatUpdate;

/**
 * @author aviolette
 * @since 10/14/18
 */
@Singleton
public class StatPullQueueServlet extends HttpServlet {

  private static final Logger log = Logger.getLogger(StatPullQueueServlet.class.getName());

  private final ObjectMapper mapper;
  private final MetricsService service;

  @Inject
  public StatPullQueueServlet(ObjectMapper mapper, MetricsService service) {
    this.mapper = mapper;
    this.service = service;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    Queue q = QueueFactory.getQueue("stats-queue");
    List<TaskHandle> tasks = q.leaseTasks(3600, TimeUnit.SECONDS, 100);
    log.log(Level.INFO, "Stats in queue: {0}", tasks.size());
    if (tasks.isEmpty()) {
      return;
    }

    Map<StatUpdate, Integer> counts = new HashMap<>();
    for (TaskHandle task : tasks) {
      try {
        StatUpdate update = mapper.readValue(task.getPayload(), StatUpdate.class);
        counts.merge(update, update.getAmount(), Integer::sum);
      } catch (Exception ex) {
        log.log(Level.INFO, "Problem serializing {0}", task.getPayload());
        log.log(Level.SEVERE, ex.getMessage(), ex);
      }
      try {
        q.deleteTask(task);
      } catch (TransientFailureException ignored) {
      }
    }
    service.updateStats(counts);
  }
}
