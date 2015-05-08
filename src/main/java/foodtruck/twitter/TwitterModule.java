package foodtruck.twitter;

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
public class TwitterModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(TwitterService.class).to(TwitterServiceImpl.class);
    // hackaroosky to get around current problem with getting twitter data in app engine
    if ("Production".equals(System.getProperty("com.google.appengine.runtime.environment")) ||
        !"true".equals(System.getProperty("remote.tweet.update"))) {
      bind(TweetCacheUpdater.class).to(LocalCacheUpdater.class);
    } else {
      bind(TweetCacheUpdater.class).to(RemoteCacheUpdater.class);
    }
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

  @FoodtruckLocatorEndpoint @Provides @Singleton
  public WebResource provideWebResource() {
    Client c = Client.create();
    c.setConnectTimeout(10000);
    c.setReadTimeout(20000);
    return c.resource("http://chicagofoodtrucklocator.appspot.com/service/tweets");
  }


  @FacebookEndpoint
  @Provides @Singleton
  public WebResource provideFacebookResource() {
    Client c = Client.create();
    return c.resource("http://graph.facebook.com");
  }

}
