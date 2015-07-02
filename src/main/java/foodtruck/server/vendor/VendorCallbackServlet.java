package foodtruck.server.vendor;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import foodtruck.server.security.SimplePrincipal;
import foodtruck.util.Session;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

/**
 * Servlet that receives the verification code from twitter and gets the screen name from the access token.
 * @author aviolette
 * @since 7/1/15
 */

@Singleton
public class VendorCallbackServlet extends HttpServlet {
  private final Provider<Session> sessionProvider;

  @Inject
  public VendorCallbackServlet(Provider<Session> sessionProvider) {
    this.sessionProvider = sessionProvider;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    Session session = sessionProvider.get();
    Twitter twitter = (Twitter) session.getProperty("twitter");
    RequestToken requestToken = (RequestToken) session.getProperty("requestToken");
    String verifier = req.getParameter("oauth_verifier");
    try {
      AccessToken token = twitter.getOAuthAccessToken(requestToken, verifier);
      String screenName = token.getScreenName();
      session.setProperty("principal", new SimplePrincipal(screenName));
      session.removeProperty("requestToken");
      session.removeProperty("twitter");
      resp.sendRedirect("/vendor");
    } catch (TwitterException e) {
      throw new ServletException(e);
    }
  }
}
