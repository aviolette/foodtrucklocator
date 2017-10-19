package foodtruck.util;

import javax.annotation.Nullable;

import com.google.common.base.Strings;
import com.google.common.escape.Escaper;
import com.google.common.html.HtmlEscapers;

/**
 * @author aviolette
 * @since 12/11/16
 */
public class FormDataMassager {
  @Nullable
  public static String escape(String data) {
    if (data == null) {
      return data;
    }
    Escaper escaper = HtmlEscapers.htmlEscaper();
    return escaper.escape(data);
  }

  @Nullable
  public static String escapeUrl(String url) {
    url = escape(url);
    if (!Strings.isNullOrEmpty(url) && !url.startsWith("http")) {
      url = "http://" + url;
    }
    return url;
  }

  @Nullable
  public static String normalizePhone(String phone) {
    phone = Strings.nullToEmpty(escape(phone));
    if(phone.length() < 10) {
      return phone;
    } else {
      phone = phone.replaceAll("\\(|\\)|\\-|\\+|\\.| ", "");
      if(phone.length() == 10) {
        return phone.substring(0, 3) + "-" + phone.substring(3, 6) + "-" + phone.substring(6, 10);
      }
    }
    return phone;
  }
}
