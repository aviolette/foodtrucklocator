package foodtruck.monitoring;

import com.google.inject.Inject;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.joda.time.DateTime;

import foodtruck.dao.SystemStatDAO;
import foodtruck.model.SystemStats;
import foodtruck.util.Clock;

/**
 * @author aviolette@gmail.com
 * @since 7/5/12
 */
public class MonitorInterceptor implements MethodInterceptor {
  private SystemStatDAO systemStatDAO;
  private Clock clock;

  @Inject
  public MonitorInterceptor() {
  }

  @Inject
  public void initialize(SystemStatDAO dao, Clock clock) {
    this.systemStatDAO = dao;
    this.clock = clock;
  }

  @Override public Object invoke(MethodInvocation invocation) throws Throwable {
    String methodName = invocation.getMethod().getName();
    String className = invocation.getMethod().getDeclaringClass().getName();
    // TODO: This is not thread-safe!
    String prefix = className + "_" + methodName;
    DateTime now = clock.now();
    try {
      return invocation.proceed();
    } catch (Exception e) {
      SystemStats stats = systemStatDAO.findByTimestamp(now);
      stats.incrementCount(prefix + "_failed");
      systemStatDAO.save(stats);
      throw e;
    } finally {
      SystemStats stats = systemStatDAO.findByTimestamp(now);
      stats.incrementCount(prefix + "_total");
      systemStatDAO.save(stats);
    }
  }
}
