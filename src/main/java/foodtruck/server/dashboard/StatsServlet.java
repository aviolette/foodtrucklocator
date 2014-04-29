package foodtruck.server.dashboard;

import java.io.IOException;
import java.util.Collection;

import javax.annotation.Nullable;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.ApplicationDAO;
import foodtruck.model.Application;

/**
 * @author aviolette@gmail.com
 * @since 7/6/12
 */
@Singleton
public class StatsServlet extends HttpServlet {

  private final ApplicationDAO applicationDAO;

  @Inject
  public StatsServlet(ApplicationDAO applicationDAO) {
    this.applicationDAO = applicationDAO;
  }

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req.setAttribute("nav", "stats");
    Collection<Application> apps = applicationDAO.findActive();
    req.setAttribute("applications",
        FluentIterable.from(apps)
            .transform(new Function<Application, String>() {
          @Nullable @Override public String apply(@Nullable Application application) {
            return "daily_schedule_request_" + application.getAppKey();
          }
        }).toList());
    req.setAttribute("applicationNames",
        FluentIterable.from(apps)
            .transform(new Function<Application, String>() {
              @Nullable @Override public String apply(@Nullable Application application) {
                return application.getName();
              }
            }).toList());
    req.getRequestDispatcher("/WEB-INF/jsp/dashboard/stats.jsp").forward(req, resp);
  }
}
