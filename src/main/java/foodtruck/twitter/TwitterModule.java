package foodtruck.twitter;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import twitter4j.TwitterFactory;

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
  }

  @Provides @Singleton
  public TwitterFactoryWrapper provideTwitterFactory() {
    return new TwitterFactoryWrapper(new TwitterFactory());
  }

  @FoodtruckLocatorEndpoint @Provides @Singleton
  public WebResource provideWebResource() {
    Client c = Client.create();
    c.setConnectTimeout(10000);
    c.setReadTimeout(20000);
    return c.resource("http://chicagofoodtrucklocator.appspot.com/service/tweets");
  }
}
