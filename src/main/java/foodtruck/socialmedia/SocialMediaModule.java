package foodtruck.socialmedia;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.google.common.base.Throwables;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import twitter4j.TwitterFactory;
import twitter4j.conf.PropertyConfiguration;

/**
 * @author aviolette@gmail.com
 * @since 10/11/11
 */
public class SocialMediaModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(TwitterService.class).to(TwitterServiceImpl.class);
    bind(ProfileSyncService.class).to(ProfileSyncServiceImpl.class);
  }
  @Provides @Singleton
  public TwitterFactoryWrapper provideTwitterFactory() {
    Properties properties = new Properties();
    InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("twitter4j.properties");
    try {
      properties.load(in);
      in.close();
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
    properties.remove(PropertyConfiguration.OAUTH_ACCESS_TOKEN);
    properties.remove(PropertyConfiguration.OAUTH_ACCESS_TOKEN_SECRET);
    return new TwitterFactoryWrapper(new TwitterFactory(), new TwitterFactory(new PropertyConfiguration(properties)));
  }


  @FacebookEndpoint
  @Provides @Singleton
  public WebResource provideFacebookResource() {
    Client c = Client.create();
    return c.resource("http://graph.facebook.com");
  }
}
