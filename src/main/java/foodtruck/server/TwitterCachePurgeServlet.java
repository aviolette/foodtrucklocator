package foodtruck.server;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.twitter.TwitterService;
import foodtruck.util.Clock;

/**
 * @author aviolette@gmail.com
 * @since 10/11/11
 */
@Singleton
public class TwitterCachePurgeServlet extends HttpServlet implements Runnable {
  private final TwitterService service;
  private final Clock clock;

  @Inject
  public TwitterCachePurgeServlet(TwitterService service, Clock clock) {
    this.service = service;
    this.clock = clock;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    run();
  }

  @Override
  public void run() {
    service.purgeTweetsBefore(clock.currentDay());
  }
}