package foodtruck.server.api;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.model.TruckStop;
import foodtruck.truckstops.FoodTruckStopService;

/**
 * Responsible for editing truck stops from the dashboard.
 * @author aviolette@gmail.com
 * @since 1/24/12
 */
@Singleton
public class AdminTruckStopServlet extends HttpServlet {
  private final FoodTruckStopService stopService;
  private final JsonReader reader;

  @Inject
  public AdminTruckStopServlet(FoodTruckStopService stopService, JsonReader reader) {
    this.stopService = stopService;
    this.reader = reader;
  }

  @Override protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    final String requestURI = req.getRequestURI();
    long stopId = Long.parseLong(requestURI.substring(requestURI.lastIndexOf("/") + 1));
    stopService.delete(stopId);
  }

  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
  }

  @Override protected void doPut(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    try {
      final String json = new String(ByteStreams.toByteArray(req.getInputStream()));
      JSONObject jsonPayload = new JSONObject(json);
      TruckStop truckStop = reader.read(jsonPayload);
      stopService.update(truckStop);
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }
}
