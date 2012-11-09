package foodtruck.server.job;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import foodtruck.dao.ConfigurationDAO;
import foodtruck.model.Configuration;
import foodtruck.twitter.TwitterService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author aviolette@gmail.com
 * @since 10/11/11
 */
@Singleton
public class TweetCacheUpdateServlet extends HttpServlet implements Runnable {
  private static final Logger log = Logger.getLogger(TweetCacheUpdateServlet.class.getName());
  private final TwitterService service;
  private final ConfigurationDAO configDAO;

  @Inject
  public TweetCacheUpdateServlet(TwitterService service, ConfigurationDAO configDAO) {
    this.service = service;
    this.configDAO = configDAO;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    run();
  }

  @Override
  public void run() {
    Configuration configuration = configDAO.find();
    if (configuration.isLocalTwitterCachingEnabled()) {
      try {
        service.updateTwitterCache();
      } catch (Exception e) {
        log.log(Level.WARNING, "Error updating twitter cache", e);
      }
    } else {
      log.info("Local twitter caching disabled");
    }
    if (configuration.isRemoteTwitterCachingEnabled()) {
      service.updateFromRemoteCache();
    }
    service.twittalyze();
  }
}