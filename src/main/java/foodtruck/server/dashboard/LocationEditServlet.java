package foodtruck.server.dashboard;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Throwables;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.dao.LocationDAO;
import foodtruck.model.Location;
import foodtruck.server.api.JsonReader;
import foodtruck.server.api.JsonWriter;

/**
 * @author aviolette@gmail.com
 * @since 2/13/12
 */
@Singleton
public class LocationEditServlet extends HttpServlet {
  private final LocationDAO locationDAO;
  private final JsonWriter writer;
  private final JsonReader reader;

  @Inject
  public LocationEditServlet(LocationDAO dao, JsonWriter writer, JsonReader reader) {
    this.locationDAO = dao;
    this.writer = writer;
    this.reader = reader;
  }

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    final String jsp = "/WEB-INF/jsp/dashboard/locationEdit.jsp";
    final String path = req.getRequestURI();
    final String keyIndex = path.substring(path.lastIndexOf("/") + 1);

    // hack required when using * patterns in guice
    req = new HttpServletRequestWrapper(req) {
      public Object getAttribute(String name) {
        if ("org.apache.catalina.jsp_file".equals(name)) {
          return jsp;
        }
        return super.getAttribute(name);
      }
    };

    Location location = locationDAO.findByKey(Long.valueOf(keyIndex));
    if (location != null) {
      try {
        req.setAttribute("location", writer.writeLocation(location, 0, true));
      } catch (JSONException e) {
        throw new RuntimeException(e);
      }
    }
    req.getRequestDispatcher(jsp).forward(req, resp);
  }

  @Override protected void doPut(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    final String json = new String(ByteStreams.toByteArray(req.getInputStream()));
    try {
      JSONObject jsonPayload = new JSONObject(json);
      Location location = reader.readLocation(jsonPayload);
      locationDAO.save(location);
      resp.setStatus(204);
    } catch (JSONException e) {
      throw Throwables.propagate(e);
    }
  }
}
