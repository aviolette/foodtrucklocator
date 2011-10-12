package foodtruck.twitter;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import twitter4j.TwitterFactory;

/**
 * @author aviolette@gmail.com
 * @since 10/11/11
 */
public class TwitterModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(TwitterService.class).to(TwitterServiceImpl.class);
  }

  @Provides @Named("foodtruck.twitter.list")
  public int provideTwitterListId() {
    return Integer.parseInt(System.getProperty("foodtruck.twitter.list"));
  }

  @Provides @Singleton
  public TwitterFactoryWrapper provideTwitterFactory() {
    return new TwitterFactoryWrapper(new TwitterFactory());
  }
}
