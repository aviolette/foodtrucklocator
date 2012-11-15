package foodtruck.schedule;

import javax.script.ScriptEngineManager;

import com.google.gdata.client.calendar.CalendarService;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

/**
 * @author aviolette@gmail.com
 * @since Jul 19, 2011
 */
public class ScheduleModule extends AbstractModule {
  @Override
  protected void configure() {
    // TODO: use assisted inject
    bind(CalendarQueryFactory.class).to(CalendarQueryFactoryImpl.class);
    bind(AddressExtractor.class).to(JavascriptAddressExtractor.class);
    bind(ScheduleStrategy.class).to(GoogleCalendar.class);
  }

  @Provides
  public CalendarService provideCalendarService() {
    CalendarService service = new CalendarService("foodtruck-app");
    service.setConnectTimeout(6000);
    return service;
  }

  @Provides
  public ScriptEngineManager provideScriptEngineManager() {
    return new ScriptEngineManager();
  }
}

