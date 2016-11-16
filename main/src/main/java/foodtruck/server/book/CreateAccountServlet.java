package foodtruck.server.book;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.google.common.base.MoreObjects;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import foodtruck.book.UserService;
import foodtruck.model.User;
import foodtruck.util.Session;

/**
 * @author aviolette
 * @since 11/14/16
 */
@Singleton
public class CreateAccountServlet extends HttpServlet {
  private final UserService userService;
  private final Provider<Session> sessionProvider;

  @Inject
  public CreateAccountServlet(UserService userService, Provider<Session> sessionProvider) {
    this.userService = userService;
    this.sessionProvider = sessionProvider;
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request.setAttribute("suffix", "-fluid");
    request.getRequestDispatcher("/WEB-INF/jsp/book/createAccount.jsp")
        .forward(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    try {
      User user = userService.createUser(request.getParameter("firstName"), request.getParameter("lastName"), request.getParameter("email"), request.getParameter("password"), request.getParameter("passwordConfirmation"));
      Session session = sessionProvider.get();
      session.setProperty("principal", user);
      response.sendRedirect(MoreObjects.firstNonNull(request.getParameter("redirect"), "/book/prepaid"));
    } catch (IllegalStateException | IllegalArgumentException e) {
      throw new WebApplicationException(Response.status(400).entity(e.getMessage()).build());
    }
  }
}
