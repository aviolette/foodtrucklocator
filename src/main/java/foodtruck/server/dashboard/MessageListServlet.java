package foodtruck.server.dashboard;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.MessageDAO;

/**
 * @author aviolette
 * @since 2/6/14
 */
@Singleton
public class MessageListServlet extends HttpServlet {
  private final MessageDAO messageDAO;

  @Inject
  public MessageListServlet(MessageDAO messageDAO) {
    this.messageDAO = messageDAO;
  }

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    final String jsp = "/WEB-INF/jsp/dashboard/messages.jsp";
    req.setAttribute("messages", messageDAO.findAll());
    req.getRequestDispatcher(jsp).forward(req, resp);
  }
}
