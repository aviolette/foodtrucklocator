package foodtruck.server.job;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.annotations.BaseUrl;
import foodtruck.dao.LocationDAO;
import foodtruck.dao.SlackWebhookDAO;
import foodtruck.model.Location;
import foodtruck.model.SlackWebhook;
import foodtruck.model.Truck;
import foodtruck.schedule.FoodTruckStopService;
import foodtruck.time.Clock;

/**
 * @author aviolette
 * @since 10/28/18
 */
@Singleton
public class SlackLunchtimeNotifications extends HttpServlet {

  private static final Logger log = Logger.getLogger(SlackLunchtimeNotifications.class.getName());

  private final SlackWebhookDAO dao;
  private final LocationDAO locationDAO;
  private final FoodTruckStopService service;
  private final Clock clock;
  private final String baseUrl;

  @Inject
  public SlackLunchtimeNotifications(SlackWebhookDAO slackWebhookDAO, LocationDAO locationDAO,
      FoodTruckStopService service, Clock clock, @BaseUrl String baseUrl) {
    this.dao = slackWebhookDAO;
    this.locationDAO = locationDAO;
    this.service = service;
    this.clock = clock;
    this.baseUrl = baseUrl;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    log.info("Sending out slack notifications");
    List<SlackWebhook> hooks = dao.findAll();
    Location location = null;
    for (SlackWebhook hook : hooks) {
      log.log(Level.INFO, "Sending slack notification for {0} to {1}",
          new Object[]{hook.getLocationName(), hook.getWebookUrl()});
      WebResource resource = Client.create()
          .resource(hook.getWebookUrl());
      if (location == null || !hook.getLocationName().equals(location.getName())) {
        location = locationDAO.findByName(hook.getLocationName())
            .orElseThrow(() -> new ServletException("Location not found: " + hook.getLocationName()));
      }
      Set<Truck> trucks = service.findTrucksNearLocation(location, clock.now());
      if (clock.dayOfWeek().isWeekend() && trucks.isEmpty()) {
        continue;
      }
      String message = trucks.isEmpty() ? "There are no trucks at " + location.getShortenedName() + " for lunch today."  :
          "These trucks are at " + location.getShortenedName() + " today for lunch: " + trucks.stream()
            .map(truck -> "<" + baseUrl + "/trucks/" + truck.getId() + "|" + truck.getName() + ">")
            .collect(Collectors.joining(", "));
      try {
        resource.type(MediaType.APPLICATION_JSON_TYPE)
            .header("Authorization", "Bearer " + hook.getAccessToken())
            .entity(new JSONObject().put("text", message))
            .post();
      } catch (com.sun.jersey.api.client.UniformInterfaceException uie) {
        if (uie.getResponse().getStatus() == 404) {
          log.log(Level.WARNING, "Removing webhook that returned 404: {0}", hook.getWebookUrl());
          dao.delete((Long) hook.getKey());
        } else {
          log.log(Level.SEVERE, uie.getMessage(), uie);
        }
      } catch (JSONException e) {
        log.log(Level.SEVERE, e.getMessage(), e);
      }
    }
  }
}
