package foodtruck.appengine.monitoring;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

import foodtruck.monitoring.CounterPublisher;
import foodtruck.monitoring.StatUpdate;
import foodtruck.time.Clock;

/**
 * @author aviolette
 * @since 10/12/18
 */
public class PullQueuePublisher implements CounterPublisher {

  private static final Logger log = Logger.getLogger(PullQueuePublisher.class.getName());

  private final ObjectMapper mapper;
  private final Clock clock;

  @Inject
  public PullQueuePublisher(ObjectMapper mapper, Clock clock) {
    this.mapper = mapper;
    this.clock = clock;
  }

  @Override
  public void increment(String name) {
    increment(name, 1);
  }

  @Override
  public void increment(String name, int amount) {
    increment(name, amount, clock.nowInMillis(), ImmutableMap.of());
  }

  @Override
  public void increment(String name, int amount, long timestampInMillis, Map<String, String> labels) {
    Queue q = QueueFactory.getQueue("stats-queue");
    try {
      String obj = mapper.writeValueAsString(new StatUpdate(name, amount, timestampInMillis, labels));
      log.log(Level.FINE, "Writing stat to queue {0}", obj);
      q.add(TaskOptions.Builder.withMethod(TaskOptions.Method.PULL).payload(obj));
    } catch (JsonProcessingException e) {
      log.log(Level.WARNING, e.getMessage(), e);
    }
  }
}
