package foodtruck.server.dashboard;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Throwables;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import static com.google.common.base.Preconditions.checkNotNull;
import foodtruck.dao.LocationDAO;
import foodtruck.model.Location;
import foodtruck.server.GuiceHackRequestWrapper;
import foodtruck.server.resources.json.LocationReader;
import foodtruck.server.resources.json.LocationWriter;
import foodtruck.truckstops.FoodTruckStopService;

/**
 * @author aviolette@gmail.com
 * @since 2/13/12
 */
@Singleton
public class LocationEditServlet extends HttpServlet {
  private final LocationDAO locationDAO;
  private final LocationWriter writer;
  private final LocationReader reader;
  private final FoodTruckStopService truckStopService;

  @Inject
  public LocationEditServlet(LocationDAO dao, LocationWriter writer, LocationReader reader,
      FoodTruckStopService truckStopService) {
    this.locationDAO = dao;
    this.writer = writer;
    this.reader = reader;
    this.truckStopService = checkNotNull(truckStopService);
  }

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    final String jsp = "/WEB-INF/jsp/dashboard/locationEdit.jsp";
    final String path = req.getRequestURI();
    final String keyIndex = path.substring(path.lastIndexOf("/") + 1);
    req = new GuiceHackRequestWrapper(req, jsp);
    Location location = locationDAO.findById(Long.valueOf(keyIndex));
    if (location != null) {
      try {
        req.setAttribute("location", writer.writeLocation(location, 0, true));
      } catch (JSONException e) {
        throw new RuntimeException(e);
      }
    }
    req.setAttribute("nav", "locations");
    req.getRequestDispatcher(jsp).forward(req, resp);
  }

  @Override protected void doPut(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    final String json = new String(ByteStreams.toByteArray(req.getInputStream()));
    try {
      JSONObject jsonPayload = new JSONObject(json);
      Location location = reader.toLocation(jsonPayload);
      locationDAO.save(location);
      truckStopService.updateLocationInCurrentSchedule(location);
      resp.setStatus(204);
    } catch (JSONException e) {
      throw Throwables.propagate(e);
    }
  }
}
