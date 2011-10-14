package foodtruck.server;

import java.io.IOException;

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
    service.updateTwitterCache();
    service.updateLocationsOfTwitterTrucks();
  }
}