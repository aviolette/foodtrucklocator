package foodtruck.linxup;

import java.util.List;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;

/**
 * @author aviolette
 * @since 11/1/16
 */
public class LinxupMapHistoryResponse {
  private List<Stop> stops;
  private List<Position> positions;

  public LinxupMapHistoryResponse(ImmutableList<Stop> stops, ImmutableList<Position> positions) {
    this.stops = stops;
    this.positions = positions;
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
        .toString();
  }
}
