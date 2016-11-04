package foodtruck.linxup;

import java.util.List;

/**
 * @author aviolette
 * @since 7/25/16
 */
class LinxupMapResponse extends LinxupResponse {
  private List<Position> positions;

  LinxupMapResponse(String type, String message) {
    super(type, message);
  }

  LinxupMapResponse(List<Position> positions) {
    this(null, null);
    this.positions = positions;
  }

  List<Position> getPositions() {
    return positions;
  }
}
