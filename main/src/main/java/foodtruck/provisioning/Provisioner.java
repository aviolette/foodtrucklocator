package foodtruck.provisioning;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import com.google.common.base.Throwables;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.PropertyConfiguration;

/**
 * This is code that outputs an authorization URL that can be used to authorize the food truck finder app with a
 * twitter account.  When that URL is hit (logged in with the target twitter account) and the user authorizes the
 * food truck finder, a PIN is given by twitter.  That PIN can then be entered to receive the access token and
 * secret.
 * @author aviolette
 * @since 12/7/12
 */
@SuppressWarnings("AppEngineForbiddenCode")
public class Provisioner {

  public static void main(String args[]) throws Exception {
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
    Twitter twitter = new TwitterFactory(new PropertyConfiguration(properties)).getInstance();
    RequestToken requestToken = twitter.getOAuthRequestToken();
    AccessToken accessToken = null;
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    while (null == accessToken) {
      System.out.println("Open the following URL and grant access to your account:");
      System.out.println(requestToken.getAuthorizationURL());
      System.out.print("Enter the PIN(if aviailable) or just hit enter.[PIN]:");
      String pin = br.readLine();
      try {
        if (pin.length() > 0) {
          accessToken = twitter.getOAuthAccessToken(requestToken, pin);
        } else {
          accessToken = twitter.getOAuthAccessToken();
        }
      } catch (TwitterException te) {
        if (401 == te.getStatusCode()) {
          System.out.println("Unable to get the access token.");
        } else {
          te.printStackTrace();
        }
      }
    }
    System.out.println("ACCESS TOKEN: " + accessToken.getToken());
    System.out.println("TOKEN SECRET: " + accessToken.getTokenSecret());
    System.exit(0);
  }
}
