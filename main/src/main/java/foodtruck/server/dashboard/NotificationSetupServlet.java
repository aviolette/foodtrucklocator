package foodtruck.server.dashboard;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import foodtruck.annotations.BaseUrl;
import foodtruck.session.Session;
import foodtruck.socialmedia.TwitterFactoryWrapper;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.RequestToken;

/**
 * @author aviolette
 * @since 10/21/16
 */
@Singleton
public class NotificationSetupServlet extends HttpServlet {

  private final Provider<TwitterFactoryWrapper> twitterFactoryProvider;
  private final Provider<Session> sessionProvider;
  private final String baseUrl;

  @Inject
  public NotificationSetupServlet(Provider<TwitterFactoryWrapper> twitterFactoryProvider,
      Provider<Session> sessionProvider, @BaseUrl String baseUrl) {
    this.twitterFactoryProvider = twitterFactoryProvider;
    this.sessionProvider = sessionProvider;
    this.baseUrl = baseUrl;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    Twitter twitter = twitterFactoryProvider.get()
        .createDetached();
    Session session = sessionProvider.get();
    try {
      RequestToken token = twitter.getOAuthRequestToken(baseUrl + "/vendor/callback");
      session.setProperty("twitter", twitter);
      session.setProperty("requestToken", token);
      resp.sendRedirect(token.getAuthenticationURL());
    } catch (TwitterException e) {
      throw new IOException(e);
    }
  }
}
