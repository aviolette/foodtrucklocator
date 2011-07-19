package foodtruck.schedule;

import java.io.FileNotFoundException;
import java.util.Map;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import foodtruck.config.TruckConfigParser;
import foodtruck.model.Truck;
import twitter4j.TwitterFactory;

/**
 * @author aviolette@gmail.com
 * @since Jul 19, 2011
 */
public class ScheduleModule extends AbstractModule {
  @Override
  protected void configure() {
  }

  @Provides @Singleton
  public TwitterFactoryWrapper provideTwitterFactory() {
    return new TwitterFactoryWrapper(new TwitterFactory());
  }

  @Provides @DefaultStrategy
  public ScheduleStrategy provideTwitterStrategy(TwitterFactoryWrapper factoryWrapper) {
    return new TwitterFeedScheduleStrategy(factoryWrapper);
  }

  @Provides @Singleton
  public Map<Truck, ScheduleStrategy> providesTruckStrategies(TruckConfigParser parser,
      @DefaultStrategy ScheduleStrategy defaultStrategy) throws FileNotFoundException {
    String url = Thread.currentThread().getContextClassLoader().getResource("trucks.yaml").getFile();
    return parser.parse(url, defaultStrategy);
  }

}
