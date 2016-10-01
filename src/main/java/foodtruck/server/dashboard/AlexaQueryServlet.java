package foodtruck.server.dashboard;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.alexa.IntentProcessor;
import foodtruck.dao.AlexaExchangeDAO;
import foodtruck.model.AlexaExchange;

/**
 * @author aviolette
 * @since 9/29/16
 */
@Singleton
public class AlexaQueryServlet extends HttpServlet {

  private static final String JSP_PATH = "/WEB-INF/jsp/dashboard/alexaQuery.jsp";
  private final AlexaExchangeDAO alexaExchangeDAO;
  private final Map<String, IntentProcessor> processors;
  private final List<String> intentNames;

  @Inject
  public AlexaQueryServlet(Map<String, IntentProcessor> processors, AlexaExchangeDAO alexaExchangeDAO) {
    this.alexaExchangeDAO = alexaExchangeDAO;
    this.processors = processors;
    this.intentNames = Ordering.natural()
        .immutableSortedCopy(processors.keySet());
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String selectedIntent = MoreObjects.firstNonNull(req.getParameter("intentName"), "GetFoodTrucksAtLocation");
    List<AlexaExchange> foodtrucksAtLocation = alexaExchangeDAO.findMostRecentOfIntent(selectedIntent);
    ImmutableList<String> slotNames = Ordering.natural()
        .immutableSortedCopy(processors.get(selectedIntent)
            .getSlotNames());
    req.setAttribute("intentNames", intentNames);
    req.setAttribute("slots", slotNames);
    req.setAttribute("intentName", selectedIntent);
    req.setAttribute("alexaResults", foodtrucksAtLocation);
    req.setAttribute("nav", "alexa");
    req.getRequestDispatcher(JSP_PATH)
        .forward(req, resp);
  }
}
