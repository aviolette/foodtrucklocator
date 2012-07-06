// Copyright 2012 BrightTag, Inc. All rights reserved.
package foodtruck.monitoring;

import com.google.inject.Inject;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

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
    SystemStats stats = systemStatDAO.findByTimestamp(clock.now());
    String prefix = className + "_" + methodName;
    stats.incrementCount(prefix + "_total");
    try {
      return invocation.proceed();
    } catch (Exception e) {
      stats.incrementCount(prefix + "_failed");
      throw e;
    } finally {
      systemStatDAO.save(stats);
    }
  }
}
