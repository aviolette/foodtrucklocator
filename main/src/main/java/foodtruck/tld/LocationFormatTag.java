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
  private static final Logger log = Logger.getLogger(LocationFormatTag.class.getName());
  private final DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYYMMdd");
  private Location location;
  private DateTime when;
  private boolean admin;
  private boolean longFormat;

  public void setAdmin(boolean admin) {
    this.admin = admin;
  }

  public DateTime getAt() {
    return when;
  }

  public void setAt(DateTime dateTime) {
    when = dateTime;
  }

  public Location getLocation() {
    return this.location;
  }

  public void setLocation(@Nullable Location location) {
    this.location = location;
  }

  public boolean isLongFormat() {
    return longFormat;
  }

  public void setLongFormat(boolean longFormat) {
    this.longFormat = longFormat;
  }

  @Override public int doStartTag() throws JspException {
    JspWriter out = pageContext.getOut();
    String dateString = "";
    if (when != null) {
      dateString = "date=" + formatter.print(when);
    }
    try {
      if (location != null) {
        String locationName = HtmlEscapers.htmlEscaper()
            .escape(location.getName());
        String aString = admin ? "/admin" : "";
        if (location.getKey() != null) {
          out.println("<a href='" + aString + "/locations/" + location.getKey() + "?" + dateString +"'>" + locationName + "</a>");
        } else {
          out.println("<a href='/locations?q=" + locationName + "&" + dateString + "'>" + name(location) + "</a>");
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

  private String name(Location location) {
    return longFormat ? location.getName() : location.getShortenedName();
  }
}
