package foodtruck.net;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.sun.jersey.api.client.Client;

import foodtruck.annotations.UserAgent;

/**
 * Just configuring Client, such that it can be configured in one place
 * @author aviolette
 * @since 2/19/18
 */
public class NetworkModule extends AbstractModule {

  @Override
  protected void configure() {
  }

  @Provides @Singleton
  public Client provideClient() {
    return Client.create();
  }

  @Provides @UserAgent
  public String providesUserAgent() {
    return "chicago food truck finder/1.0";
  }
}
