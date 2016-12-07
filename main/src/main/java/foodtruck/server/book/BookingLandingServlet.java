package foodtruck.server.book;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Singleton;

/**
 * @author aviolette
 * @since 11/11/16
 */
@Singleton
public class BookingLandingServlet extends HttpServlet {
  public static final String JSP = "/WEB-INF/jsp/book/landing.jsp";

  @Override
  protected void doGet(HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException {
    request.setAttribute("suffix", "");
    request.getRequestDispatcher(JSP)
        .forward(request, response);
  }
}
