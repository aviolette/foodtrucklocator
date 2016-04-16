package foodtruck.model;

import java.util.Set;

import javax.annotation.Nullable;

import com.google.api.client.util.Sets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

import org.joda.time.LocalDate;

import foodtruck.util.MoreStrings;

/**
 * @author aviolette
 * @since 10/26/15
 */
public class DailyData extends ModelEntity {
  private @Nullable String locationId;
  private LocalDate onDate;
  private Set<SpecialInfo> specials;
  private @Nullable String truckId;

  private DailyData(Builder builder) {
    this.key = builder.key;
    this.locationId = builder.locationId;
    this.onDate = builder.onDate;
    this.specials = ImmutableSet.copyOf(builder.specials);
    this.truckId = builder.truckId;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(DailyData dailyData) {
    return new Builder(dailyData);
  }

  public @Nullable String getLocationId() {
    return locationId;
  }

  public LocalDate getOnDate() {
    return onDate;
  }

  public boolean hasSpecials() {
    return !specials.isEmpty();
  }

  public Set<SpecialInfo> getSpecials() {
    return specials;
  }

  public @Nullable String getTruckId() {
    return truckId;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    } else if (!(obj instanceof DailyData)) {
      return false;
    }
    DailyData other = (DailyData) obj;
    boolean value =  Objects.equal(locationId, other.locationId) && Objects.equal(truckId, other.truckId)
        && other.onDate.equals(onDate) && Objects.equal(key, other.key);
    value = value && Objects.equal(Iterables.getFirst(other.specials, null), Iterables.getFirst(specials, null));
    return value;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(locationId, truckId, onDate, key, specials);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("location", locationId)
        .add("on date", onDate)
        .add("specials", specials)
        .add("key", key)
        .add("truck ID", truckId)
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
    private @Nullable String locationId;
    private LocalDate onDate;
    private Set<SpecialInfo> specials = Sets.newHashSet();
    private Object key = -1L;
    private @Nullable String truckId;

    public Builder() {}

    public Builder(DailyData dailyData) {
      this.locationId = dailyData.locationId;
      this.truckId = dailyData.truckId;
      this.onDate = dailyData.onDate;
      this.specials.clear();
      this.specials.addAll(dailyData.getSpecials());
      this.key = dailyData.key;
    }

    public Builder locationId(String locationId) {
      this.locationId = locationId;
      return this;
    }

    public Builder truckId(String truckId) {
      this.truckId = truckId;
      return this;
    }

    public Builder onDate(LocalDate date) {
      this.onDate = date;
      return this;
    }

    public Builder addSpecial(String special, boolean soldOut) {
      this.specials.add(new SpecialInfo(MoreStrings.capitalize(special), soldOut));
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
