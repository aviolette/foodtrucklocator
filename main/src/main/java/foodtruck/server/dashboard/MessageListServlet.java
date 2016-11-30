package foodtruck.server.dashboard;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.MessageDAO;
import foodtruck.time.Clock;

/**
 * @author aviolette
 * @since 2/6/14
 */
@Singleton
public class MessageListServlet extends HttpServlet {
  private final MessageDAO messageDAO;
  private final Clock clock;

  @Inject
  public MessageListServlet(MessageDAO messageDAO, Clock clock) {
    this.messageDAO = messageDAO;
    this.clock = clock;
  }

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    final String jsp = "/WEB-INF/jsp/dashboard/messages.jsp";
    if ("all".equals(req.getParameter("show"))) {
      req.setAttribute("messages", messageDAO.findAll());
      req.setAttribute("oldMessages", true);
    } else {
      req.setAttribute("messages", messageDAO.findExpiresAfter(clock.currentDay()));
      req.setAttribute("oldMessages", false);
    }
    req.setAttribute("nav", "messages");
    req.getRequestDispatcher(jsp).forward(req, resp);
  }
}
