package foodtruck.server.resources.json;

import java.lang.reflect.Type;

import javax.annotation.Nullable;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * @author aviolette@gmail.com
 * @since 4/18/12
 */
@Provider
public class DateTimeProvider extends AbstractHttpContextInjectable<DateTime>
    implements InjectableProvider<Context, Type> {
  private DateTimeFormatter timeFormatter;

  @Inject
  public DateTimeProvider(DateTimeZone zone) {
    this.timeFormatter = DateTimeFormat.forPattern("YYYYMMdd-HHmm").withZone(zone);
  }

  @Override public @Nullable DateTime getValue(HttpContext httpContext) {
    MultivaluedMap<String, String> params = httpContext.getRequest().getQueryParameters();
    final String timeRequest = params.getFirst("time");
    if (!Strings.isNullOrEmpty(timeRequest)) {
      try {
        return timeFormatter.parseDateTime(timeRequest);
      } catch (IllegalArgumentException ignored) {
      }
    }
    return null;
  }

  @Override public ComponentScope getScope() {
    return ComponentScope.PerRequest;
  }

  @Override public Injectable getInjectable(ComponentContext componentContext, Context context,
      Type type) {
    return type.equals(DateTime.class) ? this : null;
  }
}
