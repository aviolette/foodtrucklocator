package foodtruck.server.resources.json;

import javax.ws.rs.ext.Provider;

import com.google.inject.Inject;

import foodtruck.model.TruckStopWithCounts;

/**
 * @author aviolette
 * @since 4/8/15
 */
@Provider
public class TruckStopWithCountsCollectionWriter  extends CollectionWriter<TruckStopWithCounts, TruckStopWithCountsWriter> {

  @Inject
  public TruckStopWithCountsCollectionWriter(TruckStopWithCountsWriter writer) {
    super(writer, TruckStopWithCounts.class);
  }
}