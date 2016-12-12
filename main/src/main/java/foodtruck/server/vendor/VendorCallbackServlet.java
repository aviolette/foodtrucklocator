package foodtruck.server.vendor;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import foodtruck.dao.TruckDAO;
import foodtruck.dao.TwitterNotificationAccountDAO;
import foodtruck.mail.SystemNotificationService;
import foodtruck.model.LoginMethod;
import foodtruck.model.Truck;
import foodtruck.model.TwitterNotificationAccount;
import foodtruck.server.security.SimplePrincipal;
import foodtruck.session.Session;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import static foodtruck.server.vendor.VendorTwitterRedirectServlet.NOLOGON_PARAM;

/**
 * Servlet that receives the verification code from twitter and gets the screen name from the access token.
 *
 * @author aviolette
 * @since 7/1/15
 */

@Singleton
public class VendorCallbackServlet extends HttpServlet {
  private static final Logger log = Logger.getLogger(VendorCallbackServlet.class.getName());
  private final Provider<Session> sessionProvider;
  private final SystemNotificationService emailNotifier;
  private final TwitterNotificationAccountDAO twitterNotificationDAO;
  private final TruckDAO truckDAO;
  private final Provider<SessionUser> sessionUserProvider;

  @Inject
  public VendorCallbackServlet(Provider<Session> sessionProvider, SystemNotificationService emailNotifier,
      TwitterNotificationAccountDAO twitterNotificationAccountDAO, TruckDAO truckDAO,
      Provider<SessionUser> sessionUserProvider) {
    this.sessionProvider = sessionProvider;
    this.emailNotifier = emailNotifier;
    this.twitterNotificationDAO = twitterNotificationAccountDAO;
    this.truckDAO = truckDAO;
    this.sessionUserProvider = sessionUserProvider;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    Session session = sessionProvider.get();
    Twitter twitter = (Twitter) session.getProperty("twitter");
    boolean noLogon = (Boolean) session.getProperty(NOLOGON_PARAM);
    RequestToken requestToken = (RequestToken) session.getProperty("requestToken");
    String verifier = req.getParameter("oauth_verifier");
    try {
      @SuppressWarnings("ConstantConditions") AccessToken token = twitter.getOAuthAccessToken(requestToken, verifier);
      String screenName = token.getScreenName();
      TwitterNotificationAccount notificationAccount = twitterNotificationDAO.findByTwitterHandle(screenName);
      if (notificationAccount != null) {
        handleNotification(resp, token, notificationAccount);
      } else {
        screenName = screenName.toLowerCase();
        if (noLogon) {
          log.log(Level.INFO, "Linked @{0} via twitter", screenName);
        } else {
          log.log(Level.INFO, "User {0} logged on via twitter", screenName);
          session.setProperty("principal", new SimplePrincipal(screenName));
        }
        Truck truck = Iterables.getFirst(truckDAO.findByTwitterId(screenName), null);
        if (truck != null) {
          if (noLogon && !verifyWithSessionUser(truck)) {
            log.log(Level.WARNING, "Logged in user {0} is not associated with @{1}", new Object[]{truck, screenName});
            resp.sendError(400, "Logged in user is not associated with @" + screenName);
            return;
          }
          Truck.Builder truckBuilder = Truck.builder(truck);
          if (noLogon || !truck.isNeverLinkTwitter()) {
            truckBuilder.twitterToken(token.getToken())
                .twitterTokenSecret(token.getTokenSecret())
                .build();
          } else {
            truckBuilder.clearTwitterCredentials();
          }
          truckDAO.save(truckBuilder.build());
        } else if (noLogon) {
          resp.sendError(400, "Truck not associated with @" + screenName);
          return;
        }
        session.removeProperty("requestToken");
        session.removeProperty("twitter");
        if (!noLogon) {
          emailNotifier.systemNotifyVendorPortalLogin(screenName, LoginMethod.TWITTER);
        }
        resp.sendRedirect("/vendor");
      }
    } catch (TwitterException e) {
      throw new ServletException(e);
    }
  }

  private void handleNotification(HttpServletResponse resp, AccessToken token,
      TwitterNotificationAccount notificationAccount) throws IOException {
    notificationAccount = TwitterNotificationAccount.builder(notificationAccount)
        .oauthToken(token.getToken())
        .oauthTokenSecret(token.getTokenSecret())
        .build();
    twitterNotificationDAO.save(notificationAccount);
    resp.sendRedirect("/admin");
  }

  private boolean verifyWithSessionUser(Truck truck) {
    SessionUser sessionUser = sessionUserProvider.get();
    return sessionUser.associatedTrucks()
        .contains(truck);
  }
}
