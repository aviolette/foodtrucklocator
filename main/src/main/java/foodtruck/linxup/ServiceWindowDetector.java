package foodtruck.linxup;

import com.google.inject.ImplementedBy;

/**
 * @author aviolette
 * @since 2018-12-03
 */
@ImplementedBy(ServiceWindowDetectorImpl.class)
public interface ServiceWindowDetector {
  boolean during();
}
