package foodtruck.model;

import java.util.Set;

import com.google.api.client.util.Sets;
import com.google.common.collect.ImmutableSet;

import org.joda.time.LocalDate;

/**
 * @author aviolette
 * @since 10/26/15
 */
public class Specials extends ModelEntity {
  private String locationId;
  private LocalDate onDate;
  private Set<SpecialInfo> specials;

  private Specials(Builder builder) {
    this.key = builder.key;
    this.locationId = builder.locationId;
    this.onDate = builder.onDate;
    this.specials = ImmutableSet.copyOf(builder.specials);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(Specials specials) {
    return new Builder(specials);
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
  }

  public static class Builder {
    private String locationId;
    private LocalDate onDate;
    private Set<SpecialInfo> specials = Sets.newHashSet();
    private Object key;

    public Builder() {}

    public Builder(Specials specials) {
      this.locationId = specials.locationId;
      this.onDate = specials.onDate;
      this.specials.clear();
      this.specials.addAll(specials.getSpecials());
      this.key = specials.key;
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

    public Specials build() {
      return new Specials(this);
    }
  }
}
