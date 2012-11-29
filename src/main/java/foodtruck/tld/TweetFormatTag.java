package foodtruck.tld;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;

/**
 * @author aviolette
 * @since 11/29/12
 */
public class TweetFormatTag extends BodyTagSupport {
  private static final Pattern TWITTER_PATTERN = Pattern.compile("@([\\w|\\d|_]+)");

  @Override
  public int doAfterBody() throws JspException {
    BodyContent content = getBodyContent();
    String body = content.getString();
    JspWriter out = content.getEnclosingWriter();
    if(body != null) {
      try {
        out.print(formatBody(body));
      } catch (IOException e) {
        throw Throwables.propagate(e);
      }
    }
    return SKIP_BODY;
  }

  @VisibleForTesting
  static String formatBody(String body) {
    Matcher matcher = TWITTER_PATTERN.matcher(body);
    StringBuilder builder = new StringBuilder();
    int pos = 0;
    while (matcher.find()) {
      builder.append(body.substring(pos, matcher.start()));
      String twitterId = matcher.group(0);
      builder.append("<a target=\"_blank\" href=\"http://twitter.com/");
      builder.append(twitterId.substring(1));
      builder.append("\">").append(twitterId).append("</a>");
      pos = matcher.end();
    }
    builder.append(body.substring(pos));
    return builder.toString();
  }
}
