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
    resp.getWriter().write("YluCn_HdJwFJWYQUGY9Yd8CnVz9uTWbk2muMA9aE2a8.G0-9lfhFfooxK32XNFfifqqxYRWjUgHiFf32xVhm8zo");
  }
}
