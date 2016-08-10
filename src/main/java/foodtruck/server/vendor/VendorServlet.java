package foodtruck.server.vendor;

import java.io.IOException;
import java.util.List;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.base.Throwables;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.dao.LocationDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.model.Location;
import foodtruck.model.StaticConfig;
import foodtruck.model.Truck;
import foodtruck.util.Session;

/**
 * @author aviolette
 * @since 10/15/13
 */
@Singleton
public class VendorServlet extends VendorServletSupport {
  private static final String JSP = "/WEB-INF/jsp/vendor/vendordash.jsp";
  private final LocationDAO locationDAO;
  private final StaticConfig config;

  @Inject
  public VendorServlet(TruckDAO dao, LocationDAO locationDAO, Provider<Session> sessionProvider,
      UserService userService, StaticConfig config) {
    super(dao, sessionProvider, userService, locationDAO);
    this.locationDAO = locationDAO;
    this.config = config;
  }

  @Override protected void dispatchGet(HttpServletRequest req, HttpServletResponse resp, @Nullable Truck truck)
      throws ServletException, IOException {
    if (truck != null) {
      req.setAttribute("categories", new JSONArray(truck.getCategories()));
    }
    final List<Location> autocompleteLocations = locationDAO.findAutocompleteLocations();
    List<String> locationNames = ImmutableList.copyOf(Iterables.transform(autocompleteLocations, Location.TO_NAME));
    req.setAttribute("locations", new JSONArray(locationNames).toString());
    req.setAttribute("googleApiKey", config.getGoogleJavascriptApiKey());
    req.setAttribute("tab", "vendorhome");
    if (truck != null) {
      req.setAttribute("blacklist", new JSONArray(FluentIterable.from(truck.getBlacklistLocationNames())
          .transform(new Function<String, JSONObject>() {
            public JSONObject apply(String input) {
              Location location = locationDAO.findByAddress(input);
              if (location == null) {
                return null;
              }
              try {
                return new JSONObject()
                    .put("name", input)
                    .put("latitude", location.getLatitude())
                    .put("longitude", location.getLongitude())
                    .put("radius", location.getRadius());
              } catch (JSONException e) {
                throw Throwables.propagate(e);
              }
            }
          })
          .filter(Predicates.notNull())
          .toList()));
    }
    req.getRequestDispatcher(JSP).forward(req, resp);
  }
}
