package foodtruck.server.dashboard;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.EventDAO;

/**
 * @author aviolette
 * @since 5/28/13
 */
@Singleton
public class EventListServlet extends HttpServlet {
  private static final String JSP_PATH = "/WEB-INF/jsp/dashboard/events.jsp";
  private final EventDAO eventDAO;

  @Inject
  public EventListServlet(EventDAO eventDAO) {
    this.eventDAO = eventDAO;
  }

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req.setAttribute("events", eventDAO.findAll());
    req.setAttribute("nav", "events");
    req.getRequestDispatcher(JSP_PATH).forward(req, resp);
  }
}
