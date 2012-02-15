package foodtruck.server.dashboard;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;

import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.Location;
import foodtruck.server.api.JsonWriter;

/**
 * @author aviolette@gmail.com
 * @since 12/19/11
 */
@Singleton
public class LocationListServlet extends HttpServlet {
  private final GeoLocator locator;
  private final JsonWriter writer;

  @Inject
  public LocationListServlet(GeoLocator locator, JsonWriter writer) {
    this.locator = locator;
    this.writer = writer;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    final String jsp = "/WEB-INF/jsp/dashboard/locationDashboard.jsp";
    req = new HttpServletRequestWrapper(req) {
      public Object getAttribute(String name) {
        if ("org.apache.catalina.jsp_file".equals(name)) {
          return jsp;
        }
        return super.getAttribute(name);
      }
    };

    String searchField = req.getParameter("searchfield");
    JSONArray results = new JSONArray();
    if (!Strings.isNullOrEmpty(searchField)) {
      Location location = locator.locate(searchField, GeolocationGranularity.NARROW);
      if (location != null) {
        try {
          results.put(writer.writeLocation(location, 0, true));
        } catch (JSONException e) {
          throw new RuntimeException(e);
        }
      }
    }

    req.setAttribute("results", results.toString());
    req.setAttribute("nav", "locations");
    req.getRequestDispatcher(jsp).forward(req, resp);
  }
}
