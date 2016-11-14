package foodtruck.server.book;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.IOException;

import com.google.inject.Singleton;

/**
 * @author aviolette
 * @since 11/14/16
 */
@Singleton
public class CreateAccountServlet extends HttpServlet {

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    request.setAttribute("suffix", "-fluid");
    request.getRequestDispatcher("/WEB-INF/jsp/book/createAccount.jsp")
        .forward(request, response);
  }
}
