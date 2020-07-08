package foodtruck.server;


import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import foodtruck.model.StaticConfig;
import foodtruck.user.LoggedInUser;

/**
 * @author aviolette
 * @since 12/7/16
 */
@Singleton
public class PublicPageFilter implements Filter {
  private final Provider<Optional<LoggedInUser>> loggedInUserProvider;
  private final StaticConfig staticConfig;

  @Inject
  public PublicPageFilter(Provider<Optional<LoggedInUser>> loggedInUserProvider, StaticConfig staticConfig) {
    this.loggedInUserProvider = loggedInUserProvider;
    this.staticConfig = staticConfig;
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {

  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
      FilterChain filterChain) throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) servletRequest;
    Optional<LoggedInUser> loggedInUser = loggedInUserProvider.get();
    req.setAttribute("isAdmin", loggedInUser.isPresent() && loggedInUser.get()
        .isAdmin());
    String title = System.getProperty("foodtrucklocator.title", "Chicago Food Truck Finder");
    req.setAttribute("title", title);
    req.setAttribute("brandTitle", title);
    req.setAttribute("suffix", "");
    req.setAttribute("bootstrap4", true);
    req.setAttribute("googleApiKey", staticConfig.getGoogleJavascriptApiKey());
    req.setAttribute("mapButtons", System.getProperty("foodtrucklocator.map.buttons",
        "[{name:'University of Chicago', " + "latitude: 41.790628999999996, longitude:-87.60130099999999}, {name:'Downtown', latitude: 41.8806818, longitude: -87.6330294}]"));
    req.setAttribute("showBoozy", !"false".equals(System.getProperty("foodtrucklocator.showBoozy")));
    req.setAttribute("showWeekly", !"false".equals(System.getProperty("foodtrucklocator.showWeekly")));
    req.setAttribute("showAbout", !"false".equals(System.getProperty("foodtrucklocator.showAbout")));
    req.setAttribute("showStats", false);
    req.setAttribute("showBlog", !"false".equals(System.getProperty("foodtrucklocator.showBlog")));
    req.setAttribute("city", staticConfig.getCity());
    req.setAttribute("twitterHandle", System.getProperty("foodtrucklocator.twitter.handle", "chifoodtruckz"));
    req.setAttribute("facebookPage", System.getProperty("foodtrucklocator.facebook.page", "chicagofoodtruckfinder"));
    req.setAttribute("signalId", staticConfig.getSignalId());
    filterChain.doFilter(req, servletResponse);
  }

  @Override
  public void destroy() {

  }
}
