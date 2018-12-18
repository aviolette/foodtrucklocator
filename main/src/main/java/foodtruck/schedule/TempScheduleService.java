package foodtruck.schedule;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.inject.Inject;
import com.google.inject.Provider;

import foodtruck.dao.TempTruckStopDAO;

/**
 * @author aviolette
 * @since 2018-12-11
 */
public class TempScheduleService {

  private final TempTruckStopDAO dao;
  private final Provider<Queue> queueProvider;

  @Inject
  public TempScheduleService(TempTruckStopDAO dao, Provider<Queue> queueProvider) {
    this.dao = dao;
    this.queueProvider = queueProvider;
  }

  public void rebuild() {
    dao.deleteAll();
    Queue queue = queueProvider.get();
    queue.add(TaskOptions.Builder.withUrl("/cron/populate_imperial_oaks_stops"));
    queue.add(TaskOptions.Builder.withUrl("/cron/populate_coastline_cove"));
  }
}
