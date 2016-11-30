package foodtruck.appengine.monitoring;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.inject.Inject;
import com.google.inject.Provider;

import foodtruck.monitoring.CounterPublisher;

/**
 * @author aviolette
 * @since 11/30/16
 */
public class QueuePublisher implements CounterPublisher {
  private final Provider<Queue> queueProvider;

  @Inject
  public QueuePublisher(Provider<Queue> monitoringQueue) {
    this.queueProvider = monitoringQueue;
  }

  @Override
  public void increment(String statName) {
    Queue queue = queueProvider.get();
    queue.add(TaskOptions.Builder.withUrl("/cron/update_count")
        .param("statName", statName));
  }
}
