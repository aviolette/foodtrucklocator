package foodtruck.alexa;

import com.amazon.speech.speechlet.Speechlet;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.MapBinder;


/**
 * @author aviolette
 * @since 8/25/16
 */
public class AlexaModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(Speechlet.class).to(FTFSpeechlet.class);
    MapBinder<String, IntentProcessor> intentProcessorMapBinder = MapBinder.newMapBinder(binder(), String.class,
        IntentProcessor.class);
    intentProcessorMapBinder.addBinding("GetFoodTrucksAtLocation").to(LocationIntentProcessor.class);
  }
}
