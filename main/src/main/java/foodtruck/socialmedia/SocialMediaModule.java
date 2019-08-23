package foodtruck.socialmedia;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.logging.Logger;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import foodtruck.crypto.SymmetricCrypto;
import foodtruck.storage.StorageService;
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
  public TwitterFactoryWrapper provideTwitterFactory(StorageService storageService, SymmetricCrypto crypto) throws IOException {
    Properties original = findTwitterProperties(storageService, crypto);
    Properties properties = new Properties(original);
    properties.put("tweetModeExtended", true);
    properties.remove("oauth.accessToken");
    properties.remove("oauth.accessTokenSecret");
    return new TwitterFactoryWrapper(new TwitterFactory(new PropertyConfiguration(original)), new TwitterFactory(new PropertyConfiguration(properties)));
  }

  private Properties findTwitterProperties(StorageService storageService, SymmetricCrypto crypto) throws IOException {
    ByteArrayOutputStream bas = new ByteArrayOutputStream();
    storageService.readStream("cftf_secrets", "twitter4j.properties.encrypted", bas);
    byte[] propertyArray = crypto.decrypt(bas.toByteArray());
    Properties properties = new Properties();
    try (StringReader reader = new StringReader(new String(propertyArray, StandardCharsets.UTF_8))) {
      properties.load(reader);
    }
    return properties;
  }

  @FacebookEndpoint
  @Provides @Singleton
  public WebResource provideFacebookResource(Client c) {
    return c.resource("https://graph.facebook.com");
  }
}
