// Copyright 2012 BrightTag, Inc. All rights reserved.
package foodtruck.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Singleton;

/**
 * @author aviolette@gmail.com
 * @since 6/29/12
 */
@Singleton
public class TruckInfoServlet extends HttpServlet {
  @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    req.getRequestDispatcher("/WEB-INF/jsp/trucks.jsp").forward(req, resp);
  }
}
