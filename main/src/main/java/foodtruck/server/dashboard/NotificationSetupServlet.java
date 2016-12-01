package foodtruck.server.dashboard;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import foodtruck.appengine.session.Session;
import foodtruck.model.StaticConfig;
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
  private final TwitterFactoryWrapper twitterFactory;
  private final Provider<Session> sessionProvider;
  private final StaticConfig config;

  @Inject
  public NotificationSetupServlet(TwitterFactoryWrapper twitterFactory, Provider<Session> sessionProvider,
      StaticConfig config) {
    this.twitterFactory = twitterFactory;
    this.sessionProvider = sessionProvider;
    this.config = config;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    Twitter twitter = twitterFactory.createDetached();
    Session session = sessionProvider.get();
    try {
      RequestToken token = twitter.getOAuthRequestToken(config.getBaseUrl() + "/vendor/callback");
      session.setProperty("twitter", twitter);
      session.setProperty("requestToken", token);
      resp.sendRedirect(token.getAuthenticationURL());
    } catch (TwitterException e) {
      throw Throwables.propagate(e);
    }
  }

}
