package foodtruck.server.vendor;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import foodtruck.session.Session;
import foodtruck.socialmedia.TwitterFactoryWrapper;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.RequestToken;

/**
 * Servlet that gets the request token, adds the request token to the session, coookies the browser, and forwards
 * on to twitter.
 * @author aviolette
 * @since 6/30/15
 */
@Singleton
public class VendorTwitterRedirectServlet extends HttpServlet {
  static final String NOLOGON_PARAM = "nologon";
  private final TwitterFactoryWrapper twitterFactory;
  private final Provider<Session> sessionProvider;

  @Inject
  public VendorTwitterRedirectServlet(TwitterFactoryWrapper twitterFactory, Provider<Session> sessionProvider) {
    this.twitterFactory = twitterFactory;
    this.sessionProvider = sessionProvider;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    Twitter twitter = twitterFactory.createDetached();
    Session session = sessionProvider.get();
    boolean noLogon = "true".equals(req.getParameter(NOLOGON_PARAM));
    try {
      int index = req.getRequestURL().indexOf("/", 8);
      RequestToken token = twitter.getOAuthRequestToken(req.getRequestURL()
          .substring(0, index) + "/vendor/callback");
      session.setProperty("twitter", twitter);
      session.setProperty("requestToken", token);
      session.setProperty(NOLOGON_PARAM, noLogon);
      resp.sendRedirect(token.getAuthenticationURL());
    } catch (TwitterException e) {
      throw new ServletException(e);
    }
  }
}
