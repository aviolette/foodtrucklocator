// Copyright 2010 BrightTag, Inc. All rights reserved.
package foodtruck.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * A request wrapper that gets around a bug in guice that prevents it from working on
 * wild-card servlet requests.
 * @author aviolette@gmail.com
 * @since 2/21/12
 */
public class GuiceHackRequestWrapper extends HttpServletRequestWrapper {
  private final String jspName;

  public GuiceHackRequestWrapper(HttpServletRequest request, String jspName) {
    super(request);
    this.jspName = jspName;
  }

  public Object getAttribute(String name) {
    if ("org.apache.catalina.jsp_file".equals(name)) {
      return jspName;
    }
    return super.getAttribute(name);
  }
}
