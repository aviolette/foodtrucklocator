package foodtruck.server.api;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Singleton;

/**
 * @author aviolette@gmail.com
 * @since 1/24/12
 */
@Singleton
public class AdminTruckStopServlet extends HttpServlet {
  @Override protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    System.out.println("FOOBAR");
  }
}
