package foodtruck.alexa;

import java.util.Optional;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;

/**
 * @author aviolette
 * @since 1/19/18
 */
public class AddressResponse {

  private String addressLine1, addressLine2, addressLine3, districtOrCounty, stateOrRegion, city, countryCode, postalCode;

  public AddressResponse() {
  }

  private AddressResponse(Builder builder) {
    this.addressLine1 = builder.addressLine1;
    this.addressLine2 = builder.addressLine2;
    this.addressLine3 = builder.addressLine3;
    this.districtOrCounty = builder.districtOrCounty;
    this.stateOrRegion = builder.stateOrRegion;
    this.city = builder.city;
    this.countryCode = builder.countryCode;
    this.postalCode = builder.postalCode;
  }

  public String getAddressLine1() {
    return addressLine1;
  }

  public String getAddressLine2() {
    return addressLine2;
  }

  public String getAddressLine3() {
    return addressLine3;
  }

  public String getDistrictOrCounty() {
    return districtOrCounty;
  }

  public String getStateOrRegion() {
    return stateOrRegion;
  }

  public String getCity() {
    return city;
  }

  public String getCountryCode() {
    return countryCode;
  }

  public String getPostalCode() {
    return postalCode;
  }

  public Optional<String> toLocationName() {
    StringBuilder builder = new StringBuilder();
    boolean hasAddress = false;
    if (!Strings.isNullOrEmpty(addressLine1)) {
      builder.append(addressLine1);
      hasAddress = true;
    }
    if (hasAddress) {
      builder.append(", ");
    }
    if (!Strings.isNullOrEmpty(city)) {
      builder.append(city).append(", ");
    }
    if (!Strings.isNullOrEmpty(stateOrRegion)) {
      builder.append(stateOrRegion);
    }
    String result = builder.toString();
    if (Strings.isNullOrEmpty(result)) {
      return Optional.empty();
    }
    return Optional.of(result);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("address line 1", addressLine1)
        .add("address line 2", addressLine2)
        .add("address line 3", addressLine3)
        .add("district or county", districtOrCounty)
        .add("state or region", stateOrRegion)
        .add("city", city)
        .add("country code", countryCode)
        .add("postal code", postalCode)
        .toString();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String addressLine1;
    private String addressLine2;
    private String addressLine3;
    private String districtOrCounty;
    private String stateOrRegion;
    private String countryCode;
    private String postalCode;
    private String city;

    private Builder() {
    }

    public Builder addressLine1(String addressLine1) {
      this.addressLine1 = addressLine1;
      return this;
    }

    public Builder stateOrRegion(String state) {
      this.stateOrRegion = state;
      return this;
    }

    public Builder city(String city) {
      this.city = city;
      return this;
    }

    public Builder countryCode(String countryCode) {
      this.countryCode = countryCode;
      return this;
    }

    public Builder postalCode(String postalCode) {
      this.postalCode = postalCode;
      return this;
    }

    public AddressResponse build() {
      return new AddressResponse(this);
    }
  }
}
