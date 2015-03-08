package foodtruck.server.dashboard;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.dao.ApplicationDAO;
import foodtruck.dao.ConfigurationDAO;
import foodtruck.model.Application;
import foodtruck.model.Configuration;
import foodtruck.schedule.Confidence;
import foodtruck.util.RandomString;

/**
 * @author aviolette@gmail.com
 * @since 4/10/12
 */
@Singleton
public class ConfigurationServlet extends HttpServlet {
  private static final String JSP_PATH = "/WEB-INF/jsp/dashboard/configuration.jsp";
  private final ConfigurationDAO configDAO;
  private final ApplicationDAO applicationDAO;

  @Inject
  public ConfigurationServlet(ConfigurationDAO configDAO, ApplicationDAO applicationDAO) {
    this.configDAO = configDAO;
    this.applicationDAO = applicationDAO;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    Configuration config = configDAO.find();
    config = initialSetup(config);
    req.setAttribute("config", config);
    req.setAttribute("nav", "settings");
    req.getRequestDispatcher(JSP_PATH).forward(req, resp);
  }

  private Configuration initialSetup(Configuration config) {
    Configuration.Builder builder = null;
    if (Strings.isNullOrEmpty(config.getFrontDoorAppKey())) {
      if (builder == null) {
        builder = Configuration.builder(config);
      }
      if (applicationDAO.findAll().isEmpty()) {
        try {
          Application application = Application.builder().name("Front Door").description("Front Door Application Key").enabled(true).appKey(
              RandomString.nextString(8)).build();
          applicationDAO.save(application);
          builder.frontDoorAppKey(application.getAppKey());
        } catch (Exception e) {
          log(e.getMessage(), e);
        }
      }
    }
    if (builder != null) {
      config = builder.build();
      configDAO.save(config);
    }
    return config;
  }

  @Override protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    Configuration config = configDAO.find();
    List<String> notificationReceivers = ImmutableList.copyOf(Splitter.on(",")
        .trimResults()
        .omitEmptyStrings()
        .split(req.getParameter("notificationReceivers"))
        .iterator());
    config = Configuration.builder(config)
        .minimumConfidenceForDisplay(Confidence.LOW)
        .syncUrl(req.getParameter("syncUrl"))
        .syncAppKey(req.getParameter("syncAppKey"))
        .notificationSender(req.getParameter("notificationSender"))
        .frontDoorAppKey(req.getParameter("frontDoorAppKey"))
        .systemNotificationList(notificationReceivers)
        .build();

    configDAO.save(config);
    resp.sendRedirect("/admin/configuration");
  }
}
