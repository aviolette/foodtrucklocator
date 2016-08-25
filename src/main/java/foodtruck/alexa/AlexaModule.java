package foodtruck.alexa;

import com.amazon.speech.speechlet.Speechlet;
import com.google.inject.AbstractModule;

/**
 * @author aviolette
 * @since 8/25/16
 */
public class AlexaModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(Speechlet.class).to(FTFSpeechlet.class);
  }
}
