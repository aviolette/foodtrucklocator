package foodtruck.tld;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import com.google.common.net.UrlEscapers;

import foodtruck.model.Location;

/**
 * @author aviolette
 * @since 12/18/12
 */
public class LocationFormatTag extends TagSupport {
  private Location location;
  private static final Logger log = Logger.getLogger(LocationFormatTag.class.getName());

  public void setLocation(@Nullable Location location) {
    this.location = location;
  }

  public Location getLocation() {
    return this.location;
  }

  @Override public int doStartTag() throws JspException {
    JspWriter out = pageContext.getOut();
    try {
      if (location != null) {
        out.println("<a href='/locations?q=" + UrlEscapers.urlPathSegmentEscaper().escape(location.getName()) + "'>" + location.getName() + "</a>");
      }
    } catch (Exception e) {
      if (location != null) {
        log.log(Level.INFO, "Error saving location: " + location.getName());
      }
      log.log(Level.INFO, e.getMessage(), e);
      //throw Throwables.propagate(e);
    }
    return SKIP_BODY;
  }
}
