package foodtruck.monitoring;

import com.google.inject.Inject;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * @author aviolette@gmail.com
 * @since 7/5/12
 */
public class MonitorInterceptor implements MethodInterceptor {
  private CounterPublisher publisher;

  @Inject
  public MonitorInterceptor() {
  }

  @Inject
  public void initialize(CounterPublisher publisher) {
    this.publisher = publisher;
  }

  @Override public Object invoke(MethodInvocation invocation) throws Throwable {
    String methodName = invocation.getMethod().getName();
    String className = invocation.getMethod().getDeclaringClass().getName();
    String prefix = className + "_" + methodName;
    try {
      return invocation.proceed();
    } catch (Exception e) {
      publisher.increment(prefix + "_failed");
      throw e;
    } finally {
      publisher.increment( prefix + "total");
    }
  }
}
