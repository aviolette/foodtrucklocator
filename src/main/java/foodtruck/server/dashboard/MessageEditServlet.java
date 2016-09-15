package foodtruck.server.dashboard;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.dao.MessageDAO;
import foodtruck.model.Message;
import foodtruck.server.GuiceHackRequestWrapper;
import foodtruck.util.AlexaDateFormat;
import foodtruck.util.Clock;
import foodtruck.util.Link;

/**
 * @author aviolette
 * @since 2/10/14
 */
@Singleton
public class MessageEditServlet extends HttpServlet {
  private static final String JSP_PATH = "/WEB-INF/jsp/dashboard/messageEdit.jsp";
  private final MessageDAO messageDAO;
  private final DateTimeFormatter formatter;
  private final Clock clock;

  @Inject
  public MessageEditServlet(MessageDAO messageDAO, @AlexaDateFormat DateTimeFormatter formatter, Clock clock) {
    this.messageDAO = messageDAO;
    this.formatter = formatter;
    this.clock = clock;
  }

  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req = new GuiceHackRequestWrapper(req, JSP_PATH);
    Message message;
    String id = extractId(req);
    if ("new".equals(id)) {
      DateTime now = clock.now();
      message = new Message(-1L, "", now, now.plusDays(2));
    } else {
      message = messageDAO.findById(Long.parseLong(id));
    }
    req.setAttribute("message", message);
    req.setAttribute("breadcrumbs",
        ImmutableList.of(new Link("Messages", "/admin/messages"), new Link("Edit", req.getRequestURI())));
    req.setAttribute("nav", "messages");

    req.getRequestDispatcher(JSP_PATH).forward(req, resp);
  }

  @Override protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req = new GuiceHackRequestWrapper(req, JSP_PATH);
    String id = extractId(req);
    messageDAO.delete(Long.parseLong(id));
  }

  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req = new GuiceHackRequestWrapper(req, JSP_PATH);
    String id = extractId(req);
    Long obj = "new".equals(id) ? null : Long.parseLong(id);
    Message message = new Message(obj, req.getParameter("description"),
        formatter.parseDateTime(req.getParameter("startTime")),
        formatter.parseDateTime(req.getParameter("endTime")));
    messageDAO.save(message);
    resp.sendRedirect("/admin/messages");
  }

  private String extractId(HttpServletRequest req) {
    String uri = req.getRequestURI();
    return uri.substring(uri.lastIndexOf('/')+1);
  }
}
