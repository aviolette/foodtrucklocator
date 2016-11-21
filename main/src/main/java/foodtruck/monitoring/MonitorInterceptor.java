package foodtruck.monitoring;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.inject.Inject;
import com.google.inject.Provider;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import foodtruck.dao.FifteenMinuteRollupDAO;
import foodtruck.util.Clock;

/**
 * @author aviolette@gmail.com
 * @since 7/5/12
 */
public class MonitorInterceptor implements MethodInterceptor {
  private FifteenMinuteRollupDAO fifteenMinuteRollupDAO;
  private Clock clock;
  private Provider<Queue> monitorQueueProvider;

  @Inject
  public MonitorInterceptor() {
  }

  @Inject
  public void initialize(FifteenMinuteRollupDAO dao, Clock clock, Provider<Queue> monitorQueue) {
    this.fifteenMinuteRollupDAO = dao;
    this.clock = clock;
    this.monitorQueueProvider = monitorQueue;
  }

  @Override public Object invoke(MethodInvocation invocation) throws Throwable {
    String methodName = invocation.getMethod().getName();
    String className = invocation.getMethod().getDeclaringClass().getName();
    String prefix = className + "_" + methodName;
    Queue queue = monitorQueueProvider.get();
    try {
      return invocation.proceed();
    } catch (Exception e) {
      queue.add(TaskOptions.Builder.withUrl("/cron/update_count")
          .param("statName", prefix + "_failed"));
      throw e;
    } finally {
      queue.add(TaskOptions.Builder.withUrl("/cron/update_count")
          .param("statName", prefix + "total"));
    }
  }
}
