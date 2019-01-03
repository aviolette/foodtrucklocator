package foodtruck.appengine.monitoring;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.inject.Inject;

import foodtruck.monitoring.CounterPublisher;
import foodtruck.monitoring.StatUpdate;

/**
 * @author aviolette
 * @since 10/12/18
 */
public class PullQueuePublisher implements CounterPublisher {

  private static final Logger log = Logger.getLogger(PullQueuePublisher.class.getName());

  private final ObjectMapper mapper;

  @Inject
  public PullQueuePublisher(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public void increment(String name) {
    increment(name, 1);
  }

  @Override
  public void increment(String name, int amount) {
    Queue q = QueueFactory.getQueue("stats-queue");
    try {
      String obj = mapper.writeValueAsString(new StatUpdate(name, amount));
      log.log(Level.FINE, "Writing stat to queue {0}", obj);
      q.add(TaskOptions.Builder.withMethod(TaskOptions.Method.PULL).payload(obj));
    } catch (JsonProcessingException e) {
      log.log(Level.WARNING, e.getMessage(), e);
    }
  }
}
