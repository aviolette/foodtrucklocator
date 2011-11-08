package foodtruck.twitter;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.inject.Inject;
import com.sun.jersey.api.client.WebResource;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.model.TweetSummary;
import foodtruck.server.JsonWriter;

/**
 * @author aviolette@gmail.com
 * @since 10/26/11
 */
public class RemoteCacheUpdater implements TweetCacheUpdater {
  private static final Logger log = Logger.getLogger(RemoteCacheUpdater.class.getName());
  private final WebResource foodtruckLocator;
  private final JsonWriter jsonWriter;

  @Inject
  public RemoteCacheUpdater(@FoodtruckLocatorEndpoint WebResource foodtruckLocator,
      JsonWriter writer) {
    this.foodtruckLocator = foodtruckLocator;
    this.jsonWriter = writer;
  }

  @Override
  public void update(List<TweetSummary> summaries) {
    try {
      JSONObject obj = new JSONObject();
      JSONArray arr = new JSONArray();
      for (TweetSummary summary : summaries) {
        arr.put(toJSON(summary));
      }
      obj.put("tweets", arr);
      log.log(Level.INFO, "Posting json: " + obj.toString());
      foodtruckLocator.post(obj);
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }

  private JSONObject toJSON(TweetSummary tweet) throws JSONException {
    JSONObject loc =
        (tweet.getLocation() == null) ? null : jsonWriter.writeLocation(tweet.getLocation());
    return new JSONObject()
        .put("text", tweet.getText())
        .put("time", tweet.getTime().getMillis())
        .put("tweetId", tweet.getId())
        .put("location", loc)
        .put("screenName", tweet.getScreenName());
  }
}
