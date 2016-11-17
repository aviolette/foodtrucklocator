package foodtruck.server.book;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;
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
public class LoginServlet extends HttpServlet {

  private final Provider<Session> sessionProvider;
  private final UserService userService;

  @Inject
  public LoginServlet(Provider<Session> sessionProvider, UserService userService) {
    this.sessionProvider = sessionProvider;
    this.userService = userService;
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request.getRequestDispatcher("/WEB-INF/jsp/book/signin.jsp")
        .forward(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String email = request.getParameter("email");
    String password = request.getParameter("password");
    if (Strings.isNullOrEmpty(email) || Strings.isNullOrEmpty(password)) {
      response.sendError(400, "User name and password need to be specified");
      return;
    }
    User user;
    if ((user = userService.verifyLogin(email, password)) != null) {
      sessionProvider.get()
          .setProperty("principal", user);
      response.sendRedirect(MoreObjects.firstNonNull(request.getParameter("redirect"), "/book/prepaid"));
      return;
    }
    sessionProvider.get()
        .invalidate();
    response.sendError(403, "User name or password is incorrect");
  }
}
