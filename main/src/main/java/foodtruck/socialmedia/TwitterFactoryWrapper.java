package foodtruck.socialmedia;

import java.util.Properties;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.PropertyConfiguration;

/**
 * Had to create this so I could mock out TwitterFactory (which is marked as final and I don't want
 * to have to use PowerMock).
 * @author aviolette@gmail.com
 * @since Jul 15, 2011
 */
public class TwitterFactoryWrapper {
  private final TwitterFactory factoryDetached;
  private final TwitterFactory factory;
  private final Properties properties;

  TwitterFactoryWrapper(TwitterFactory factory, TwitterFactory factoryDetached, Properties properties) {
    this.factory = factory;
    this.factoryDetached = factoryDetached;
    this.properties = properties;
  }

  /**
   * Creates a <code>Twitter</code> instance with the stored credentials
   * @return a twitter instance
   */
  public Twitter create() {
    return factory.getInstance();
  }

  public Twitter createDetached() {
    TwitterFactory twitterFactory = new TwitterFactory(new PropertyConfiguration(properties));
    return twitterFactory.getInstance();
  }

  Twitter createDetached(AccessToken accessToken) {
    return factoryDetached.getInstance(accessToken);
  }

  Twitter createDetached(PropertyConfiguration propertyConfiguration) {
    return new TwitterFactory(propertyConfiguration).getInstance();
  }
}
