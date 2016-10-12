package foodtruck.alexa;

import java.util.List;

import com.amazon.speech.speechlet.Speechlet;
import com.google.common.base.Splitter;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Named;

import foodtruck.model.Location;


/**
 * @author aviolette
 * @since 8/25/16
 */
public class AlexaModule extends AbstractModule {
  static final String GET_FOOD_TRUCKS_AT_LOCATION = "GetFoodTrucksAtLocation";
  static final String WHERE_IS_TRUCK = "WhereIsTruck";
  static final String DAILY_SPECIALS = "DailySpecials";
  static final String ABOUT_TRUCK = "AboutTruck";
  static final String CATEGORY_SEARCH = "CategorySearch";
  static final String TESTING_MODE = "foodtrucklocator.alexa.testing.mode";

  private static final Splitter LOCATION_SPLITTER = Splitter.on(";")
      .omitEmptyStrings()
      .trimResults();

  @Override
  protected void configure() {
    bind(Speechlet.class).to(FTFSpeechlet.class);
    MapBinder<String, IntentProcessor> intentProcessorMapBinder = MapBinder.newMapBinder(binder(), String.class,
        IntentProcessor.class);
    intentProcessorMapBinder.addBinding(GET_FOOD_TRUCKS_AT_LOCATION)
        .to(LocationIntentProcessor.class);
    intentProcessorMapBinder.addBinding(WHERE_IS_TRUCK)
        .to(TruckLocationIntentProcessor.class);
    intentProcessorMapBinder.addBinding(DAILY_SPECIALS)
        .to(SpecialIntentProcessor.class);
    intentProcessorMapBinder.addBinding("AMAZON.HelpIntent")
        .to(HelpIntentProcessor.class);
    intentProcessorMapBinder.addBinding("AMAZON.CancelIntent")
        .to(CancelProcessor.class);
    intentProcessorMapBinder.addBinding("AMAZON.StopIntent")
        .to(CancelProcessor.class);
    intentProcessorMapBinder.addBinding(ABOUT_TRUCK)
        .to(AboutIntentProcessor.class);
    intentProcessorMapBinder.addBinding(CATEGORY_SEARCH)
        .to(CategoryIntent.class);
  }

  // TODO: probably should actually be somewhere else (such as the servlet config) but this isn't used
  // anywhere else yet, so its here.

  @Provides
  @DefaultCenter
  public Location provideDefaultLocation() {
    String location = System.getProperty("foodtrucklocator.map.center",
        "Clark and Monroe, Chicago, IL; 41.880187; -87.63083499999999");
    List<String> splitInfo = LOCATION_SPLITTER.splitToList(location);
    return Location.builder()
        .name(splitInfo.get(0))
        .lat(Double.parseDouble(splitInfo.get(1)))
        .lng(Double.parseDouble(splitInfo.get(2)))
        .build();
  }

  @Provides
  @Named(TESTING_MODE)
  public boolean providesTestingMode() {
    return "true".equals(System.getProperty(TESTING_MODE, "false"));
  }
}
