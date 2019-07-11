package foodtruck.socialmedia;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import twitter4j.TwitterFactory;
import twitter4j.conf.PropertyConfiguration;

/**
 * @author aviolette@gmail.com
 * @since 10/11/11
 */
public class SocialMediaModule extends AbstractModule {
  private static final Logger log = Logger.getLogger(SocialMediaModule.class.getName());

  @Override
  protected void configure() {
    Multibinder<SocialMediaConnector> connectorBinder = Multibinder.newSetBinder(binder(), SocialMediaConnector.class);
    connectorBinder.addBinding().to(TwitterConnector.class);
  }

  @Provides @Singleton
  public TwitterFactoryWrapper provideTwitterFactory() throws IOException {
    Properties properties = new Properties();
    InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("twitter4j.properties");
    properties.load(in);
    in.close();
    properties.put("tweetModeExtended", true);
    properties.remove("oauth.accessToken");
    properties.remove("oauth.accessTokenSecret");
    return new TwitterFactoryWrapper(new TwitterFactory(), new TwitterFactory(new PropertyConfiguration(properties)));
  }

  @FacebookEndpoint
  @Provides @Singleton
  public WebResource provideFacebookResource(Client c) {
    return c.resource("https://graph.facebook.com");
  }
}
