package foodtruck.server.vendor;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import foodtruck.dao.TwitterNotificationAccountDAO;
import foodtruck.email.EmailNotifier;
import foodtruck.model.TwitterNotificationAccount;
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
  private static final Logger log = Logger.getLogger(VendorCallbackServlet.class.getName());
  private final Provider<Session> sessionProvider;
  private final EmailNotifier emailNotifier;
  private final TwitterNotificationAccountDAO twitterNotificationDAO;

  @Inject
  public VendorCallbackServlet(Provider<Session> sessionProvider, EmailNotifier emailNotifier,
      TwitterNotificationAccountDAO twitterNotificationAccountDAO) {
    this.sessionProvider = sessionProvider;
    this.emailNotifier = emailNotifier;
    this.twitterNotificationDAO = twitterNotificationAccountDAO;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    Session session = sessionProvider.get();
    Twitter twitter = (Twitter) session.getProperty("twitter");
    RequestToken requestToken = (RequestToken) session.getProperty("requestToken");
    String verifier = req.getParameter("oauth_verifier");
    try {
      @SuppressWarnings("ConstantConditions")
      AccessToken token = twitter.getOAuthAccessToken(requestToken, verifier);
      String screenName = token.getScreenName();
      TwitterNotificationAccount notificationAccount = twitterNotificationDAO.findByTwitterHandle(screenName);
      if (notificationAccount != null) {
        notificationAccount = TwitterNotificationAccount.builder(notificationAccount)
            .oauthToken(token.getToken())
            .oauthTokenSecret(token.getTokenSecret())
            .build();
        twitterNotificationDAO.save(notificationAccount);
        resp.sendRedirect("/admin");
      } else {
        log.log(Level.INFO, "User {0} logged on via twitter", screenName);
        session.setProperty("principal", new SimplePrincipal(screenName));
        session.removeProperty("requestToken");
        session.removeProperty("twitter");
        emailNotifier.systemNotifyVendorPortalLogin(screenName, LoginMethod.TWITTER);
        resp.sendRedirect("/vendor");
      }
    } catch (TwitterException e) {
      throw new ServletException(e);
    }
  }
}
