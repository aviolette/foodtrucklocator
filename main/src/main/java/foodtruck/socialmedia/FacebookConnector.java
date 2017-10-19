package foodtruck.socialmedia;

import java.net.URI;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.sun.jersey.api.client.WebResource;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.dao.TruckDAO;
import foodtruck.model.StaticConfig;
import foodtruck.model.Story;
import foodtruck.model.StoryType;
import foodtruck.model.Truck;
import foodtruck.time.Clock;
import foodtruck.time.FacebookTimeFormat;
import foodtruck.util.Secondary;
import foodtruck.util.ServiceException;

/**
 * @author aviolette
 * @since 8/26/15
 */
public class FacebookConnector implements SocialMediaConnector {
  private static final Logger log = Logger.getLogger(FacebookConnector.class.getName());
  private final WebResource facebook;
  private final StaticConfig config;
  private final Clock clock;
  private final DateTimeFormatter formatter;
  private final TruckDAO truckDAO;

  @Inject
  public FacebookConnector(@FacebookEndpoint WebResource facebookEndpoint, StaticConfig config,
       Clock clock, @FacebookTimeFormat DateTimeFormatter formatter,
      @Secondary TruckDAO truckDAO) {
    this.facebook = facebookEndpoint;
    this.config = config;
    this.clock = clock;
    this.formatter = formatter;
    this.truckDAO = truckDAO;
  }

  @Override
  public List<Story> recentStories() {
    ImmutableList.Builder<Story> stories = ImmutableList.builder();
    for (Truck truck : truckDAO.findFacebookTrucks()) {
      try {
        stories.addAll(retrieveStoriesForTruck(truck));
      } catch (Exception e) {
        log.log(Level.WARNING, e.getMessage(), e);
      }
    }
    return stories.build();
  }

  @Override
  public void updateStatusFor(ScheduleMessage message, Truck truck) throws ServiceException {
    // TODO: implement
  }

  private ImmutableList<Story> retrieveStoriesForTruck(Truck truck) throws JSONException {
    JSONObject json = facebook.uri(URI.create("/v2.4" + truck.getFacebook() + "/posts"))
        .queryParam("access_token", config.getFacebookAccessToken())
        .header("Accept", "application/json")
        .get(JSONObject.class);
    JSONArray data = json.getJSONArray("data");
    DateTime start = clock.currentDay().toDateTimeAtStartOfDay();
    ImmutableList.Builder<Story> stories = ImmutableList.builder();
    boolean first = true;
    for (int i=0; i < data.length(); i++) {
      JSONObject storyObj = data.getJSONObject(i);

      if (!storyObj.has("message")) {
        continue;
      }
      String id = storyObj.getString("id");

      if (id.equals(truck.getLastScanned())) {
        break;
      }

      if (first) {
        truck = truck.append().lastScanned(id).build();
        truckDAO.save(truck);
        first = false;
      }

      DateTime createTime = formatter.parseDateTime(storyObj.getString("created_time"));
      if (createTime.isBefore(start)) {
        break;
      }
      createTime = clock.now();
      String message = storyObj.getString("message");
      stories.add(Story.builder()
          .text(message)
          // This is a bad assumption to make, but the easiest way to get it to work across the board
          .userId(truck.getTwitterHandle())
          .time(createTime)
          .type(StoryType.FACEBOOK)
          .build());
    }
    return stories.build();
  }
}
