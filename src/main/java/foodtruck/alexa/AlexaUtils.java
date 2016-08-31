package foodtruck.alexa;

import java.util.List;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.collect.FluentIterable;

/**
 * @author aviolette
 * @since 8/27/16
 */
class AlexaUtils {
  private static final Joiner JOINER = Joiner.on(", ");
  private static final Joiner JOINER_WITH_PAUSE = Joiner.on(",<break time=\"0.3s\"/> ");

  static String toAlexaList(List<String> list, boolean insertPause) {
    if (list.isEmpty()) {
      return "";
    }
    String rac = list.get(list.size() - 1);
    if (list.size() == 1) {
      return rac;
    }
    String rdc;
    if (insertPause) {
      rdc = JOINER_WITH_PAUSE.join(list.subList(0, list.size() - 1));
    } else {
      rdc = JOINER.join(list.subList(0, list.size() - 1));
    }
    String oxfordComma = (list.size() == 2) ? "" : ",";
    return rdc + oxfordComma + " and " + rac;
  }

  static String intentToString(Intent intent) {
    return MoreObjects.toStringHelper(intent)
        .add("name", intent.getName())
        .add("slots", FluentIterable.from(intent.getSlots().values()).transform(new Function<Slot, String>() {
          @Override
          public String apply(Slot input) {
            return MoreObjects.toStringHelper(input)
                .add("slot", input.getName())
                .add("value", input.getValue())
                .toString();
          }
        }).join(Joiner.on("\n")))
        .toString();
  }
}
