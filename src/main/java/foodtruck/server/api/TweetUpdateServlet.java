package foodtruck.server.api;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import foodtruck.dao.ConfigurationDAO;
import foodtruck.dao.TweetCacheDAO;
import foodtruck.model.Location;
import foodtruck.model.TweetSummary;

/**
 * This was conceived when appengine was having problems connecting to twitter as a way to upload
 * tweets.
 * @author aviolette@gmail.com
 * @since 10/26/11
 */
@Singleton
public class TweetUpdateServlet extends HttpServlet {
  private static final Logger log = Logger.getLogger(TweetUpdateServlet.class.getName());
  private final DateTimeZone zone;
  private final TweetCacheDAO tweetCacheDAO;
  private final ConfigurationDAO configDAO;

  @Inject
  public TweetUpdateServlet(DateTimeZone zone, TweetCacheDAO dao,
      ConfigurationDAO configDAO) {
    this.zone = zone;
    this.tweetCacheDAO = dao;
    this.configDAO = configDAO;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    if (!configDAO.find().isTweetUpdateServletEnabled()) {
      log.log(Level.WARNING, "Attempt to call tweet updater when it is disabled");
      resp.setStatus(404);
      return;
    }
    try {
      JSONObject json = new JSONObject(new String(ByteStreams.toByteArray(req.getInputStream())));
      log.log(Level.INFO, "Loading tweets: {0}", json);
      JSONArray tweets = json.getJSONArray("tweets");
      ImmutableList.Builder<TweetSummary> summaries = ImmutableList.builder();
      for (int i = 0; i < tweets.length(); i++) {
        JSONObject tweet = tweets.getJSONObject(i);
        JSONObject location = tweet.optJSONObject("location");
        Location loc = null;
        if (location != null) {
          // TODO: add location
        }
        TweetSummary summary = new TweetSummary.Builder()
            .text(tweet.getString("text"))
            .time(new DateTime(tweet.getLong("time"), zone))
            .id(tweet.getLong("tweetId"))
            .location(loc)
            .userId(tweet.getString("screenName"))
            .build();
        summaries.add(summary);
      }
      tweetCacheDAO.save(summaries.build());
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }
}
