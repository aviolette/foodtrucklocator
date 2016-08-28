package foodtruck.alexa;

import com.amazon.speech.speechlet.Speechlet;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;


/**
 * @author aviolette
 * @since 8/25/16
 */
public class AlexaModule extends AbstractModule {
  static final String GET_FOOD_TRUCKS_AT_LOCATION = "GetFoodTrucksAtLocation";
  static final String WHERE_IS_TRUCK = "WhereIsTruck";

  @Override
  protected void configure() {
    bind(Speechlet.class).to(FTFSpeechlet.class);
    MapBinder<String, IntentProcessor> intentProcessorMapBinder = MapBinder.newMapBinder(binder(), String.class,
        IntentProcessor.class);
    intentProcessorMapBinder.addBinding(GET_FOOD_TRUCKS_AT_LOCATION).to(LocationIntentProcessor.class);
    intentProcessorMapBinder.addBinding(WHERE_IS_TRUCK).to(TruckLocationIntentProcessor.class);
    intentProcessorMapBinder.addBinding("AMAZON.HelpIntent").to(HelpIntentProcessor.class);
    intentProcessorMapBinder.addBinding("AMAZON.CancelIntent").to(CancelProcessor.class);
    intentProcessorMapBinder.addBinding("AMAZON.StopIntent").to(CancelProcessor.class);
  }
}
