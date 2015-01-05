package foodtruck.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.joda.time.DateTimeZone;

import foodtruck.dao.ConfigurationDAO;
import foodtruck.dao.EventDAO;
import foodtruck.model.StaticConfig;
import foodtruck.util.Clock;

/**
 * @author aviolette
 * @since 5/30/13
 */
@Singleton
public class EventsServlet extends FrontPageServlet {
  private final Clock clock;
  private final DateTimeZone zone;
  private final EventDAO eventDAO;

  @Inject
  public EventsServlet(ConfigurationDAO configurationDAO, EventDAO dao, Clock clock, DateTimeZone zone,
      StaticConfig staticConfig) {
    super(configurationDAO, staticConfig);
    this.eventDAO = dao;
    this.clock = clock;
    this.zone = zone;
  }

  @Override protected void doGetProtected(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    final String requestURI = req.getRequestURI();
    String eventId = (requestURI.equals("/events") || requestURI.equals("/events/") ? null : requestURI.substring(8));
    String jsp = Strings.isNullOrEmpty(eventId) ? "/WEB-INF/jsp/events.jsp" : "/WEB-INF/jsp/event.jsp";
    req = new GuiceHackRequestWrapper(req, jsp);
    req.setAttribute("tab", "events");
    if (Strings.isNullOrEmpty(eventId)) {
      req.setAttribute("events", eventDAO.findEventsAfter(clock.now()));
    } else {
      req.setAttribute("event", eventDAO.findById(eventId));
    }
    req.setAttribute("containerType", "fixed");
    req.getRequestDispatcher(jsp).forward(req, resp);
  }
}