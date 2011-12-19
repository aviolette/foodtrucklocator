package foodtruck.server.dashboard;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.model.Trucks;

/**
 * @author aviolette@gmail.com
 * @since 10/23/11
 */
@Singleton
public class DashboardServlet extends HttpServlet {
  private final Trucks trucks;

  @Inject
  public DashboardServlet(Trucks trucks) {
    this.trucks = trucks;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req = new HttpServletRequestWrapper(req) {
      public Object getAttribute(String name) {
        if ("org.apache.catalina.jsp_file".equals(name)) {
          String path = super.getServletPath();
          return path;
        }
        return super.getAttribute(name);
      }
    };
    req.setAttribute("trucks", Trucks.BY_NAME.immutableSortedCopy(trucks.allTrucks()));
    req.getRequestDispatcher("/WEB-INF/jsp/dashboard/dashboard.jsp").forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    super.doPost(req, resp);
  }
}
