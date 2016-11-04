package foodtruck.linxup;

import com.google.common.base.Strings;

/**
 * @author aviolette
 * @since 11/3/16
 */
public abstract class LinxupResponse {
  private String error;
  private String errorType;

  LinxupResponse(String type, String message) {
    errorType = type;
    error = message;
  }

  public boolean isSuccessful() {
    return Strings.isNullOrEmpty(error);
  }

  public String getError() {
    return error;
  }

  public String getErrorType() {
    return errorType;
  }
}
