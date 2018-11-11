package foodtruck.tld;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import com.google.common.escape.Escaper;
import com.google.common.net.PercentEscaper;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import foodtruck.model.Location;

/**
 * @author aviolette
 * @since 11/11/18
 */
public class DateFormatTag extends TagSupport {
  private static final Logger log = Logger.getLogger(LocationFormatTag.class.getName());
  private DateTime date;
  private String style;

  public DateTime getAt() {
    return date;
  }

  public void setAt(DateTime dateTime) {
    date = dateTime;
  }

  public String getStyle() {
    return style;
  }

  public void setStyle(String style) {
    this.style = style;
  }

  @Override public int doStartTag() throws JspException {
    DateTimeFormatter formatter = DateTimeFormat.forStyle(getStyle());
    JspWriter out = pageContext.getOut();
    try {
      out.println(formatter.print(date));
    } catch (IOException e) {
      log.log(Level.WARNING, e.getMessage(), e);
    }
    return SKIP_BODY;
  }
}
