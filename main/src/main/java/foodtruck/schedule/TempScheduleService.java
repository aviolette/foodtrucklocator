package foodtruck.schedule;

import com.google.appengine.api.taskqueue.Queue;
import com.google.inject.Inject;
import com.google.inject.Provider;

import foodtruck.dao.TempTruckStopDAO;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

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
    queue.add(withUrl("/cron/populate_imperial_oaks_stops"));
    queue.add(withUrl("/cron/populate_coastline_cove"));
    queue.add(withUrl("/cron/populate_skeleton_key"));
    queue.add(withUrl("/cron/populate_pollyanna_schedule"));
  }
}
