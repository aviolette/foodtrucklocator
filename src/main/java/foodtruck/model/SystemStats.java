package foodtruck.model;

import java.util.Map;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * @author aviolette@gmail.com
 * @since 7/5/12
 */
public class SystemStats extends ModelEntity {
  private long timeStamp;
  private Map<String, Long> attributes;

  public SystemStats(long id, long timeStamp, Map<String, Long> attributes) {
    super(id);
    this.timeStamp = timeStamp;
    this.attributes = Maps.newHashMap(attributes);
  }

  public long getTimeStamp() {
    return timeStamp;
  }

  public Map<String, Long> getAttributes() {
    return ImmutableMap.copyOf(attributes);
  }

  public long getStat(String statName) {
    if (attributes.containsKey(statName)) {
      return attributes.get(statName);
    }
    return 0L;
  }

  @Override public String toString() {
    return Objects.toStringHelper(this)
        .add("timeStamp", timeStamp)
        .add("attributes", attributes)
        .toString();
  }

  public SystemStats merge(SystemStats prev) {
    for (Map.Entry<String, Long> attr : prev.getAttributes().entrySet()) {
      if (attributes.containsKey(attr.getKey())) {
        attributes.put(attr.getKey(), attributes.get(attr.getKey()) + attr.getValue());
      } else {
        attributes.put(attr.getKey(), attr.getValue());
      }
    }
    return this;
  }
}
