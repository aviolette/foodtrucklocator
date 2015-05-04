package foodtruck.server.migrations;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.ApplicationDAO;
import foodtruck.model.Application;

/**
 * @author aviolette
 * @since 5/4/15
 */
@Singleton
public class ForceSaveApplication extends HttpServlet {
  private final ApplicationDAO applicationDAO;

  @Inject
  public ForceSaveApplication(ApplicationDAO applicationDAO) {
    this.applicationDAO = applicationDAO;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    for (Application application : applicationDAO.findAll()) {
      applicationDAO.save(application);
    }
  }
}
