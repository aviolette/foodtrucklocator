package foodtruck.server.resources;

import java.util.logging.Logger;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.google.common.base.Throwables;
import com.google.inject.Inject;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.notifications.PublicEventNotificationService;

/**
 * @author aviolette
 * @since 9/13/14
 */
@Path("/tweets")
@Produces("application/json")
public class TweetsResource {
  private static final Logger log = Logger.getLogger(TweetsResource.class.getName());
  private final PublicEventNotificationService notifier;

  @Inject
  public TweetsResource(PublicEventNotificationService notifier) {
    this.notifier = notifier;
  }

  @Path("retweets") @POST
  public void retweet(JSONObject payload) {
    try {
      notifier.retweet(payload.getString("account"), payload.getString("tweetId"));
    } catch (JSONException e) {
      throw Throwables.propagate(e);
    }
  }
}
