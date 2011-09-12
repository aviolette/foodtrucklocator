package foodtruck.server;

import java.io.IOException;
import java.net.URL;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.inject.Singleton;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import foodtruck.model.TruckLocationGroup;

/**
 * @author aviolette@gmail.com
 * @since 9/8/11
 */
@Singleton
public class FoodTruckScheduleServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    URL url = null;

    resp.setHeader("Content-Type", "application/json");
    resp.getWriter().println(req.getPathInfo());
  }

}
