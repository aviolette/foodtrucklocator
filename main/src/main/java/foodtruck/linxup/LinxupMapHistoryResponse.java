package foodtruck.linxup;

import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

/**
 * @author aviolette
 * @since 11/1/16
 */
public class LinxupMapHistoryResponse extends LinxupResponse {
  private List<Stop> stops;
  private List<Position> positions;

  LinxupMapHistoryResponse(ImmutableList<Stop> stops, ImmutableList<Position> positions) {
    super(null, null);
    this.stops = stops;
    this.positions = positions;
  }

  LinxupMapHistoryResponse(String errorType, String message) {
    super(errorType, message);
    this.stops = ImmutableList.of();
    this.positions = ImmutableList.of();
  }

  public List<Stop> getStops() {
    return stops;
  }

  public List<Position> getPositions() {
    return positions;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("stops", stops)
        .add("positions", positions)
        .add("errorType", getErrorType())
        .add("message", getError())
        .toString();
  }
}
