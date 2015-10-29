package foodtruck.model;

import java.util.Set;

import com.google.api.client.util.Sets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import org.joda.time.LocalDate;

/**
 * @author aviolette
 * @since 10/26/15
 */
public class DailyData extends ModelEntity {
  private String locationId;
  private LocalDate onDate;
  private Set<SpecialInfo> specials;

  private DailyData(Builder builder) {
    this.key = builder.key;
    this.locationId = builder.locationId;
    this.onDate = builder.onDate;
    this.specials = ImmutableSet.copyOf(builder.specials);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(DailyData dailyData) {
    return new Builder(dailyData);
  }

  public String getLocationId() {
    return locationId;
  }

  public LocalDate getOnDate() {
    return onDate;
  }

  public Set<SpecialInfo> getSpecials() {
    return specials;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else if (!(obj instanceof DailyData)) {
      return false;
    }
    DailyData other = (DailyData) obj;
    boolean value =  other.locationId.equals(locationId) && other.onDate.equals(onDate) && Objects.equal(key,
        other.key);
    value = value && Objects.equal(Iterables.getFirst(other.specials, null), Iterables.getFirst(specials, null));
    return value;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(locationId, onDate, key, specials);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("location", locationId)
        .add("on date", onDate)
        .add("specials", specials)
        .add("key", key)
        .toString();
  }

  public DailyData markAllSoldOut() {
    Builder builder = DailyData.builder(this);
    builder.clearSpecials();
    for (SpecialInfo info : specials) {
      builder.addSpecial(info.getSpecial(), true);
    }
    return builder.build();
  }

  public static class SpecialInfo {
    private boolean soldOut;
    private String special;

    public SpecialInfo(String special, boolean soldOut) {
      this.soldOut = soldOut;
      this.special = special;
    }

    public boolean isSoldOut() {
      return soldOut;
    }

    public String getSpecial() {
      return special;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      } else if (!(obj instanceof SpecialInfo)) {
        return false;
      }

      SpecialInfo other = (SpecialInfo) obj;
      return other.soldOut == soldOut && other.special.equals(special);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(soldOut, special);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("sold out", soldOut)
          .add("special", special)
          .toString();
    }
  }

  public static class Builder {
    private String locationId;
    private LocalDate onDate;
    private Set<SpecialInfo> specials = Sets.newHashSet();
    private Object key = -1L;

    public Builder() {}

    public Builder(DailyData dailyData) {
      this.locationId = dailyData.locationId;
      this.onDate = dailyData.onDate;
      this.specials.clear();
      this.specials.addAll(dailyData.getSpecials());
      this.key = dailyData.key;
    }

    public Builder locationId(String locationId) {
      this.locationId = locationId;
      return this;
    }

    public Builder onDate(LocalDate date) {
      this.onDate = date;
      return this;
    }

    public Builder addSpecial(String special, boolean soldOut) {
      this.specials.add(new SpecialInfo(special, soldOut));
      return this;
    }

    public Builder key(Object key) {
      this.key = key;
      return this;
    }

    public DailyData build() {
      return new DailyData(this);
    }

    public Builder clearSpecials() {
      this.specials.clear();
      return this;
    }
  }
}
