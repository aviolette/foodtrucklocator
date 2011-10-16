package foodtruck.server;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.twitter.TwitterService;

/**
 * @author aviolette@gmail.com
 * @since 10/11/11
 */
@Singleton
public class TweetCacheUpdateServlet  extends HttpServlet implements Runnable {
  private static final Logger log = Logger.getLogger(TweetCacheUpdateServlet.class.getName());
  private final TwitterService service;

  @Inject
  public TweetCacheUpdateServlet(TwitterService service) {
    this.service = service;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    run();
  }

  @Override
  public void run() {
    try {
      service.updateTwitterCache();
    } catch (Exception e) {
      log.log(Level.WARNING, "Error updating twitter cache", e);
    }
    service.updateLocationsOfTwitterTrucks();
  }
}