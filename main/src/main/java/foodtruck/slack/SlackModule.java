package foodtruck.slack;

import java.io.IOException;
import java.util.Properties;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import foodtruck.server.security.PropertyStore;

public class SlackModule extends AbstractModule {

  @Singleton
  @Provides
  @Named("slack.properties")
  public Properties providesProperties(PropertyStore propertyStore) throws IOException {
    return propertyStore.findProperties("slack");
  }

  @Provides
  @Named(SlackWebhooksImpl.CLIENT_ID)
  public String providesSlackClientId(@Named("slack.properties") Properties properties) {
    return properties.getProperty("client.id");
  }

  @Provides
  @Named(SlackWebhooksImpl.CLIENT_SECRET)
  public String providesSlackClientSecret(@Named("slack.properties") Properties properties) {
    return properties.getProperty("client.secret");
  }

  @Override
  protected void configure() {
    bind(SlackWebhooks.class).to(SlackWebhooksImpl.class);
  }
}
