// Copyright 2012 BrightTag, Inc. All rights reserved.
package foodtruck.dao;

import java.util.List;

import org.joda.time.DateTime;

import foodtruck.model.SystemStats;

/**
 * @author aviolette@gmail.com
 * @since 7/5/12
 */
public interface SystemStatDAO extends DAO<Long, SystemStats> {
  /**
   * Finds an stats object by its time stamp.  If none is found, it is created and the new object
   * is returned
   * @param timeStamp a time stamp
   * @return the system stats.
   */
  SystemStats findByTimestamp(DateTime timeStamp);

  List<SystemStats> findWithinRange(long startTime, long endTime);
}
