package foodtruck.tld;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import com.google.common.html.HtmlEscapers;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.model.Location;

/**
 * @author aviolette
 * @since 12/18/12
 */
public class LocationFormatTag extends TagSupport {
  private Location location;
  private static final Logger log = Logger.getLogger(LocationFormatTag.class.getName());
  private DateTime when;
  private boolean admin;
  private final DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYYMMdd");

  public void setAdmin(boolean admin) {
    this.admin = admin;
  }

  public void setAt(DateTime dateTime) {
    when = dateTime;
  }

  public DateTime getAt() {
    return when;
  }

  public void setLocation(@Nullable Location location) {
    this.location = location;
  }

  public Location getLocation() {
    return this.location;
  }

  @Override public int doStartTag() throws JspException {
    JspWriter out = pageContext.getOut();
    String dateString = "";
    if (when != null) {
      dateString = "date=" + formatter.print(when);
    }
    try {
      if (location != null) {
        String locationName = HtmlEscapers.htmlEscaper().escape(location.getName());
        String aString = admin ? "/admin" : "";
        if (location.getKey() != null) {
          out.println("<a href='" + aString + "/locations/" + location.getKey() + "?" + dateString +"'>" + locationName + "</a>");
        } else {
          out.println("<a href='/locations?q=" + locationName + "&" + dateString + "'>" + locationName + "</a>");
        }
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
