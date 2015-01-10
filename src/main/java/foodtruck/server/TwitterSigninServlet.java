package foodtruck.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import foodtruck.dao.ConfigurationDAO;
import foodtruck.model.StaticConfig;
import foodtruck.twitter.TwitterFactoryWrapper;
import foodtruck.util.Session;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.RequestToken;

/**
 * @author aviolette
 * @since 7/26/14
 */
@Singleton
public class TwitterSigninServlet extends FrontPageServlet {
  private final TwitterFactoryWrapper twitterFactory;
  private final Provider<Session> sessionProvider;

  @Inject
  public TwitterSigninServlet(ConfigurationDAO configDAO, TwitterFactoryWrapper twitterFactory,
      Provider<Session> sessionProvider, StaticConfig staticConfig) {
    super(configDAO, staticConfig);
    this.twitterFactory = twitterFactory;
    this.sessionProvider = sessionProvider;
  }

  @Override protected void doGetProtected(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    Twitter twitter = twitterFactory.createDetached();
    Session session = sessionProvider.get();
    try {
      StringBuffer callbackURL = req.getRequestURL();
      int index = callbackURL.indexOf("/");
      callbackURL.replace(index, callbackURL.length(), "").append("/petition/600w/signin/twitter_callback");
      RequestToken token = twitter.getOAuthRequestToken(callbackURL.toString());
      session.setProperty("twitter", twitter);
      session.setProperty("twitterToken", token.getToken());
      resp.sendRedirect(token.getAuthenticationURL());
    } catch (TwitterException e) {
      throw Throwables.propagate(e);
    }
  }
}
