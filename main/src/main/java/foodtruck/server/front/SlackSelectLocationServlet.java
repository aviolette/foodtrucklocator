package foodtruck.server.front;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import foodtruck.dao.LocationDAO;
import foodtruck.dao.SlackWebhookDAO;
import foodtruck.model.Location;
import foodtruck.model.SlackWebhook;
import foodtruck.session.Session;

/**
 * @author aviolette
 * @since 11/4/18
 */
@Singleton
public class SlackSelectLocationServlet extends HttpServlet {

  private final Provider<Session> sessionProvider;
  private final LocationDAO locationDAO;
  private final SlackWebhookDAO slackWebhookDAO;

  @Inject
  public SlackSelectLocationServlet(Provider<Session> sessionProvider, LocationDAO locationDAO,
      SlackWebhookDAO slackWebhookDAO) {
    this.sessionProvider = sessionProvider;
    this.locationDAO = locationDAO;
    this.slackWebhookDAO = slackWebhookDAO;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    Session session = sessionProvider.get();
    String team = (String) session.getProperty("slackTeam");
    if (Strings.isNullOrEmpty(team)) {
      resp.sendError(401, "Unauthorized");
      return;
    }
    req.setAttribute("locations", locationDAO.findPopularLocations());
    req.getRequestDispatcher("/WEB-INF/jsp/slackSelect.jsp")
        .forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    Session session = sessionProvider.get();
    String team = (String) session.getProperty("slackTeam");
    if (Strings.isNullOrEmpty(team)) {
      resp.sendError(401, "Unauthorized");
      return;
    }

    String locationId = req.getParameter("location");

    if (Strings.isNullOrEmpty(locationId)) {
      resp.sendError(400, "Location not specified");
      return;
    }

    Location location = locationDAO.findByIdOpt(Long.parseLong(locationId))
        .orElseThrow(() -> new ServletException("Invalid location specified: " + locationId));

    SlackWebhook webhook = slackWebhookDAO.findByTeamId(team)
        .orElseThrow(() -> new ServletException("Not valid team ID" + team));

    webhook = SlackWebhook.builder(webhook).locationName(location.getName()).build();
    slackWebhookDAO.save(webhook);

    resp.sendRedirect("/slack/setup_complete");
  }
}
