package foodtruck.server.resources;

import javax.annotation.Nullable;

/**
 * @author aviolette@gmail.com
 * @since 6/16/12
 */
public class ErrorPayload {
  private final String message;
  private final @Nullable String extra;

  public ErrorPayload(String message) {
    this(message, null);
  }

  public ErrorPayload(String message, String extraData) {
    this.message = message;
    this.extra = extraData;
  }

  public String getMessage() {
    return message;
  }

  @Nullable
  public String getExtra() {
    return extra;
  }
}
