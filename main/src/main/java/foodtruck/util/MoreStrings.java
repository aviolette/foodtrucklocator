package foodtruck.util;

import javax.annotation.Nullable;

import com.google.common.base.Strings;

/**
 * @author aviolette@gmail.com
 * @since 7/28/12
 */
public class MoreStrings {
  public static String capitalize(String s) {
    if(s.length() < 2) {
      return s.toUpperCase();
    }
    StringBuilder builder = new StringBuilder(s.length());
    boolean sectionBreak = true;
    for (char c : s.toCharArray()) {
      if (sectionBreak) {
        builder.append(Character.toUpperCase(c));
        sectionBreak = false;
      } else {
        if (Character.isSpaceChar(c)) {
          sectionBreak = true;
        }
        builder.append(c);
      }
    }
    return builder.toString();
  }

  @Nullable
  public static String firstNonEmpty(String s1, String s2) {
    return Strings.isNullOrEmpty(s1) ? s2 : s1;
  }
}
