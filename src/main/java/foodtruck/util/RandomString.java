package foodtruck.util;

import java.util.Random;

/**
 * @author aviolette
 * @since 1/26/13
 */
public class RandomString {

  private static final Random random = new Random();
  private static char possibleCharacters[] = new char[62];

  static {
    for (int i=0; i < 10; i++) {
      possibleCharacters[i] = (char) ('0' + i);
    }
    for (int i=0; i < 26; i++) {
      possibleCharacters[i + 10] = (char) ('A' + i);
    }
    for (int i=0; i < 26; i++) {
      possibleCharacters[i + 36] = (char) ('a' + 1);
    }
  }

  public static String nextString(int length) {
    if (length < 1) {
      return "";
    }
    char chars[] = new char[length];
    for (int i=0; i < length; i++) {
      int num = random.nextInt(61);
      chars[i] = possibleCharacters[num];
    }
    return new String(chars);
  }

}
