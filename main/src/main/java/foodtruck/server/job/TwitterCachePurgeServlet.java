package foodtruck.server.job;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import foodtruck.socialmedia.SocialMediaCacher;
import foodtruck.util.Clock;

/**
 * Removes tweets prior to midnight on the current day.
 * @author aviolette@gmail.com
 * @since 10/11/11
 */
@Singleton
public class TwitterCachePurgeServlet extends HttpServlet implements Runnable {
  private final SocialMediaCacher service;
  private final Clock clock;

  @Inject
  public TwitterCachePurgeServlet(SocialMediaCacher service, Clock clock) {
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
    service.purgeBefore(clock.currentDay());
  }
}