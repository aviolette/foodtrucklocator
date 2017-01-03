package foodtruck.server.security;

import java.lang.annotation.Annotation;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.google.common.base.Strings;
import com.google.inject.Inject;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import foodtruck.annotations.AppKey;
import foodtruck.dao.ApplicationDAO;
import foodtruck.model.Application;
import foodtruck.monitoring.Counter;
import foodtruck.monitoring.DailyScheduleCounter;
import foodtruck.monitoring.HourlyScheduleCounter;

/**
 * @author aviolette
 * @since 12/14/16
 */
class RequiresAppKeyWithCountRestrictionChecker implements MethodInterceptor {
  private static final Logger log = Logger.getLogger(RequiresAdminChecker.class.getName());
  private Counter dailyCounter;
  private Counter hourlyCounter;
  private ApplicationDAO applicationDAO;

  @Nullable
  public static Object findMatchingParameter(Object[] arguments, Annotation[][] annotations,
      Class<? extends Annotation> clazz) {
    for (int i = 0; i < annotations.length; i++) {
      Annotation[] ans = annotations[i];
      for (Annotation an : ans) {
        if (an.annotationType()
            .equals(clazz)) {
          return arguments[i];
        }
      }
    }
    return null;
  }

  @Inject
  public void setApplicationDAO(ApplicationDAO applicationDAO) {
    this.applicationDAO = applicationDAO;
  }

  @Inject
  public void setDailyCounter(@DailyScheduleCounter Counter dailyCounter) {
    this.dailyCounter = dailyCounter;
  }

  @Inject
  public void setHourlyCounter(@HourlyScheduleCounter Counter hourlyCounter) {
    this.hourlyCounter = hourlyCounter;
  }

  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    String appKey = getAppKey(invocation);
    dailyCounter.increment(appKey);
    hourlyCounter.increment(appKey);
    requireAppKeyWithCount(appKey, hourlyCounter.getCount(appKey), dailyCounter.getCount(appKey));
    return invocation.proceed();
  }

  private String getAppKey(MethodInvocation invocation) {
    return (String) findMatchingParameter(invocation.getArguments(), invocation.getMethod()
        .getParameterAnnotations(), AppKey.class);
  }

  private void requireAppKeyWithCount(String appKey, long hourlyCount, long dailyCount) throws WebApplicationException {
    if (!Strings.isNullOrEmpty(appKey)) {
      Application app = applicationDAO.findById(appKey);
      if (app != null && app.isEnabled()) {
        if (app.isRateLimitEnabled() && (hourlyCount > 6 || dailyCount > 100)) {
          log.warning("Rate limit exceeded for " + app.getName());
          throw new WebApplicationException(Response.Status.UNAUTHORIZED);
        }
        log.info("Requesting application: " + app.getName());
        return;
      }
    }
    log.warning("App key not specified");
    throw new WebApplicationException(Response.Status.UNAUTHORIZED);
  }
}