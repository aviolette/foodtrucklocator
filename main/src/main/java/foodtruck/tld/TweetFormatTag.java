package foodtruck.tld;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.google.common.annotations.VisibleForTesting;

/**
 * @author aviolette
 * @since 11/29/12
 */
public class TweetFormatTag extends BodyTagSupport {
  private static final Pattern URL_PATTERN = Pattern.compile("https?:\\/\\/([-\\w\\.]+)+(:\\d+)?(\\/([\\w/_\\.]*(\\?\\S+)?)?)?");
  private static final Pattern TWITTER_PATTERN = Pattern.compile("@([\\w|\\d|_]+)");

  @VisibleForTesting
  static String formatBody(String body) {
    body = sanitizeHtml(body);
    body = formatUrls(body);
    return formatTwitterHandles(body);
  }

  private static String sanitizeHtml(String body) {
    body = body.replaceAll(" &", " &amp;");
    body = body.replaceAll("<", "&lt;");
    return body.replaceAll(">", "&gt;");
  }

  private static String formatUrls(String body) {
    Matcher matcher = URL_PATTERN.matcher(body);
    StringBuilder builder = new StringBuilder();
    int pos = 0;
    while (matcher.find()) {
      builder.append(body.substring(pos, matcher.start()));
      String url = matcher.group(0);
      builder.append("<a target=\"_blank\" href=\"");
      builder.append(url);
      builder.append("\">").append(url).append("</a>");
      pos = matcher.end();
    }
    builder.append(body.substring(pos));
    return builder.toString();
  }

  private static String formatTwitterHandles(String body) {
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

  @Override
  public int doAfterBody() throws JspException {
    BodyContent content = getBodyContent();
    String body = content.getString();
    JspWriter out = content.getEnclosingWriter();
    if (body != null) {
      try {
        out.print(formatBody(body));
      } catch (IOException e) {
        throw new JspException(e);
      }
    }
    return SKIP_BODY;
  }
}
