package foodtruck.twitter;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;

/**
 * Had to create this so I could mock out TwitterFactory (which is marked as final and I don't want
 * to have to use PowerMock).
 * @author aviolette@gmail.com
 * @since Jul 15, 2011
 */
public class TwitterFactoryWrapper {
  private TwitterFactory factory;

  public TwitterFactoryWrapper(TwitterFactory factory) {
    this.factory = factory;
  }
  public Twitter create() {
    return factory.getInstance();
  }
}
