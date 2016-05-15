package foodtruck.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Singleton;

/**
 * @author aviolette
 * @since 2/14/16
 */
@Singleton
public class SSLVerificationServlet extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    resp.getWriter().write("MZxAdOD7MXGB2Z1vQz2csYqXp_ul9DU9iqLnPipiTfU.G0-9lfhFfooxK32XNFfifqqxYRWjUgHiFf32xVhm8zo");
  }
}
