package foodtruck.server.resources;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.google.common.base.Throwables;
import com.google.inject.Inject;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.dao.TwitterNotificationAccountDAO;
import foodtruck.model.TwitterNotificationAccount;
import foodtruck.twitter.TwitterFactoryWrapper;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

/**
 * @author aviolette
 * @since 9/13/14
 */
@Path("/tweets")
@Produces("application/json")
public class TweetsResource {

  private final TwitterNotificationAccountDAO notificationAccountDAO;
  private final TwitterFactoryWrapper twitterFactoryWrapper;

  @Inject
  public TweetsResource(TwitterNotificationAccountDAO notificationAccountDAO, TwitterFactoryWrapper twitterFactoryWrapper) {
    this.notificationAccountDAO = notificationAccountDAO;
    this.twitterFactoryWrapper = twitterFactoryWrapper;
  }

  @Path("retweets") @POST
  public void retweet(JSONObject payload) {
    try {
      final String accountName = payload.getString("account");
      TwitterNotificationAccount account = notificationAccountDAO.findByTwitterHandle(accountName);
      if (account == null) {
        throw new WebApplicationException(Response.status(400).entity("Account not found: " + accountName).build());
      }
      Twitter twitter = new TwitterFactory(account.twitterCredentials()).getInstance();
      twitter.retweetStatus(payload.getLong("tweetId"));
    } catch (TwitterException e) {
      throw Throwables.propagate(e);
    } catch (JSONException e) {
      throw Throwables.propagate(e);
    }

  }
}
