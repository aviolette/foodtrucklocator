package foodtruck.server.vendor;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.users.UserService;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import foodtruck.model.StaticConfig;
import foodtruck.server.FrontPageServlet;

/**
 * @author aviolette
 * @since 11/20/16
 */
@Singleton
public class VendorInfoServlet extends FrontPageServlet {

  @Inject
  public VendorInfoServlet(StaticConfig staticConfig, Provider<UserService> userServiceProvider) {
    super(staticConfig);
  }

  @Override
  protected void doGetProtected(HttpServletRequest request,
      HttpServletResponse response) throws ServletException, IOException {
    request.setAttribute("title", "Vendor Information");
    request.setAttribute("website", System.getProperty("foodtrucklocator.title", "Chicago Food Truck Finder"));
    request.getRequestDispatcher("/WEB-INF/jsp/vendor/info.jsp")
        .forward(request, response);
  }
}
