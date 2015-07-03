package foodtruck.server.vendor;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import foodtruck.util.Session;

/**
 * @author aviolette
 * @since 7/3/15
 */
@Singleton
public class VendorLogoutServlet extends HttpServlet {
  private final Provider<Session> sessionProvider;

  @Inject
  public VendorLogoutServlet(Provider<Session> sessionProvider) {
    this.sessionProvider = sessionProvider;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    Session session = sessionProvider.get();
    session.invalidate();
    resp.sendRedirect("/vendor");
  }
}
