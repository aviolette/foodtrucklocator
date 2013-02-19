package foodtruck.facebook;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

/**
 * A module that provide's services that connect with Facebook's API.
 * @author aviolette
 * @since 2/18/13
 */
public class FacebookModule extends AbstractModule {

  @Override protected void configure() {
    bind(FacebookService.class).to(FacebookServiceImpl.class);
  }

  @FacebookEndpoint @Provides @Singleton
  public WebResource provideFacebookResource() {
    Client c = Client.create();
    return c.resource("http://graph.facebook.com");
  }
}
