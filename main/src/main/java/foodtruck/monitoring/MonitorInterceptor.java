package foodtruck.monitoring;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import foodtruck.time.Clock;

/**
 * @author aviolette@gmail.com
 * @since 7/5/12
 */
public class MonitorInterceptor implements MethodInterceptor {
  private CounterPublisher publisher;
  private Clock clock;

  @Inject
  public MonitorInterceptor() {
  }

  @Inject
  public void initialize(CounterPublisher publisher, Clock clock) {
    this.publisher = publisher;
    this.clock = clock;
  }

  @Override public Object invoke(MethodInvocation invocation) throws Throwable {
    String methodName = invocation.getMethod().getName();
    String className = invocation.getMethod().getDeclaringClass().getName();
    String prefix = className + "_" + methodName;
    try {
      return invocation.proceed();
    } catch (Exception e) {
      publisher.increment(prefix, 1, clock.nowInMillis(), ImmutableMap.of("COUNT_TYPE", "FAILED"));
      throw e;
    } finally {
      publisher.increment( prefix, 1, clock.nowInMillis(), ImmutableMap.of("COUNT_TYPE", "TOTAL"));
    }
  }
}
