package foodtruck.schedule.custom.nyc;

import java.util.logging.Logger;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;

import foodtruck.schedule.SpecialMatcher;
import foodtruck.schedule.Spot;

/**
 * @author aviolette
 * @since 10/6/16
 */
public class NewYorkModule extends AbstractModule {
  private static final Logger log = Logger.getLogger(NewYorkModule.class.getName());

  @Override
  protected void configure() {
    log.info("Installing matching binders for NYC");
    MapBinder<String, SpecialMatcher> binder = MapBinder.newMapBinder(binder(), String.class, SpecialMatcher.class);
    binder.addBinding("usafoodtruck")
        .to(USATruckMatcher.class);
    binder.addBinding("gcnyc1")
        .to(GorillaCheeseNYCMatcher.class);
  }

  @Provides
  @Singleton
  public ImmutableList<Spot> provideCommonSpots() {
    return ImmutableList.of(new Spot("slip/water", "Old Slip and Water, New York, NY"), new Spot("dumbo", "DUMBO"),
        new Spot("water/jay", "Water and Jay, New York, NY"), new Spot("park/33", "Park and 33rd, New York, NY"),
        new Spot("91/columbus", "91th and Columbus, New York, NY"),
        new Spot("broadway/55", "Broadway and 55th, New York, NY"),
        new Spot("52/madison", "52nd and Madison, New York, NY"),
        new Spot("madison/98", "Madison and 98th, New York, NY"),
        new Spot("60th/columbus", "60th and Columbus, New York, NY"),
        new Spot("madison/58", "Madison and 58th, New York, NY"),
        new Spot("49th st in between 6th and 7th ave", "49th and 6th, New York, NY"),
        new Spot("49th/6th", "49th and 6th, New York, NY"), new Spot("6th/broadway", "49th and Broadway, New York, NY"),
        new Spot("30-30 47th ave", "30-30 47th Ave, Long Island City, NY"),
        new Spot("hudson/king", "Hudson and King, New York, NY"), new Spot("metrotech", "metrotech"));
  }
}
