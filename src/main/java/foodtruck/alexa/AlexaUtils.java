package foodtruck.alexa;

import java.util.List;

import com.google.common.base.Joiner;

/**
 * @author aviolette
 * @since 8/27/16
 */
class AlexaUtils {
  private static final Joiner JOINER = Joiner.on(", ");

  static String toAlexaList(List<String> list) {
    if (list.isEmpty()) {
      return "";
    }
    String rac = list.get(list.size() - 1);
    if (list.size() == 1) {
      return rac;
    }
    String rdc = JOINER.join(list.subList(0, list.size() - 1));
    String oxfordComma = (list.size() == 2) ? "" : ",";
    return rdc + oxfordComma + " and " + rac;
  }
}
