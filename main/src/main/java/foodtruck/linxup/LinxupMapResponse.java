package foodtruck.linxup;

import java.util.List;

import com.google.common.base.Strings;

/**
 * @author aviolette
 * @since 7/25/16
 */
class LinxupMapResponse {
  private List<Position> positions;
  private String error;
  private String errorType;

  LinxupMapResponse(String type, String message) {
    errorType = type;
    error = message;
  }

  LinxupMapResponse(List<Position> positions) {
    this.positions = positions;
  }

  boolean isSuccessful() {
    return Strings.isNullOrEmpty(error);
  }

  List<Position> getPositions() {
    return positions;
  }

  public String getError() {
    return error;
  }
}
