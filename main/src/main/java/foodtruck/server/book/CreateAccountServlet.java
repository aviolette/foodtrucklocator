package foodtruck.server.book;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.MoreObjects;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import foodtruck.session.Session;
import foodtruck.book.PasswordHasher;
import foodtruck.book.UserService;
import foodtruck.model.User;

/**
 * @author aviolette
 * @since 11/14/16
 */
@Singleton
public class CreateAccountServlet extends HttpServlet {
  private final UserService userService;
  private final Provider<Session> sessionProvider;
  private final PasswordHasher hasher;

  @Inject
  public CreateAccountServlet(UserService userService, Provider<Session> sessionProvider, PasswordHasher hasher) {
    this.userService = userService;
    this.sessionProvider = sessionProvider;
    this.hasher = hasher;
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request.setAttribute("suffix", "-fluid");
    request.getRequestDispatcher("/WEB-INF/jsp/book/createAccount.jsp")
        .forward(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String email = request.getParameter("email");
    try {
      String confirmation = hasher.hash(request.getParameter("confirmPassword"));
      User user = userService.createUser(User.builder()
          .email(email)
          .firstName(request.getParameter("firstName"))
          .lastName(request.getParameter("lastName"))
          .hashedPassword(hasher.hash(request.getParameter("password")))
          .build(), confirmation);
      Session session = sessionProvider.get();
      session.setProperty("principal", user);
      response.sendRedirect(MoreObjects.firstNonNull(request.getParameter("redirect"), "/book/prepaid"));
    } catch (IllegalStateException | IllegalArgumentException e) {
      response.sendError(400, e.getMessage());
    }
  }
}
