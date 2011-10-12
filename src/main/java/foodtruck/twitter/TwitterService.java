package foodtruck.twitter;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

/**
 * Service for retrieving and caching tweets.
 * @author aviolette@gmail.com
 * @since 10/11/11
 */
public interface TwitterService {

  /**
   * Puts all tweets after the specified time for all the trucks in the twitter cache.
   * @param startTime the time of the first tweet to be included in the list.
   */
  void updateTwitterFeedsFor(DateTime startTime);

  void purgeTweetsBefore(LocalDate localDate);
}
