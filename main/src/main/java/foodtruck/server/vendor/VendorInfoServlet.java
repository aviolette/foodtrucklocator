package foodtruck.server.vendor;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Singleton;

/**
 * @author aviolette
 * @since 11/20/16
 */
@Singleton
public class VendorInfoServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException {
    request.setAttribute("title", "Vendor Information");
    request.setAttribute("suffix", "");
    request.setAttribute("website", System.getProperty("foodtrucklocator.title", "Chicago Food Truck Finder"));
    request.getRequestDispatcher("/WEB-INF/jsp/vendor/info.jsp")
        .forward(request, response);
  }
}
