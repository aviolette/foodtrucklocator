package foodtruck.linxup;

import com.google.inject.Exposed;
import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;

/**
 * @author aviolette
 * @since 7/21/16
 */
public class LinxupModule extends PrivateModule {
  private static final String USERNAME = "linxup.username";
  private static final String PASSWORD = "linxup.password";

  @Override
  protected void configure() {
    bind(LinxupConnector.class).to(LinxupConnectorImpl.class);
    expose(LinxupConnector.class);
  }

  @Provides @Exposed
  public LinxupMapRequest provideMapRequest(@Named(USERNAME) String userName,
      @Named(PASSWORD) String password) {
    return new LinxupMapRequest(userName, password);
  }

  @LinxupEndpoint @Provides @Exposed
  public WebResource provideEndpoint() {
    DefaultClientConfig clientConfig = new DefaultClientConfig(LinxupMapResponseProvider.class,
        LinxupMapRequestWriter.class);
    return Client.create(clientConfig)
      .resource("https://www.linxup.com/ibis/rest/linxupmobile/map");
  }

  @Provides @Named(USERNAME)
  public String providesUsername() {
    return System.getProperty("foodtrucklocator.linxup.username");
  }

  @Provides @Named(PASSWORD)
  public String providesPassword() {
    return  System.getProperty("foodtrucklocator.linxup.password");
  }
}