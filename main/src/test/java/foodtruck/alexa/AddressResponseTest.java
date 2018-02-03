package foodtruck.alexa;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.truth.Truth8;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author aviolette
 * @since 2/3/18
 */
@RunWith(MockitoJUnitRunner.class)
public class AddressResponseTest {

  /**
   * Not really a unit test...just making sure jackson can load this properly
   */
  @Test
  public void fromJson() throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    AddressResponse response = mapper.readValue("{\"addressLine1\":\"123 Main Street\",\"addressLine2\":null,\"addressLine3\":null,\"districtOrCounty\":\"Cook\",\"stateOrRegion\":\"IL\",\"city\":\"Hoffman Estates\",\"countryCode\":\"US\",\"postalCode\":\"60010\"}", AddressResponse.class);
    assertThat(response.getAddressLine1()).isEqualTo("123 Main Street");
    assertThat(response.getDistrictOrCounty()).isEqualTo("Cook");
    assertThat(response.getStateOrRegion()).isEqualTo("IL");
    assertThat(response.getPostalCode()).isEqualTo("60010");
    assertThat(response.getCity()).isEqualTo("Hoffman Estates");
    assertThat(response.getCountryCode()).isEqualTo("US");
  }

  @Test
  public void toLocationName() {
    AddressResponse resp = AddressResponse.builder()
        .addressLine1("123 Main Street")
        .stateOrRegion("IL")
        .city("Hoffman Estates")
        .countryCode("US")
        .postalCode("60010")
        .build();
    Truth8.assertThat(resp.toLocationName()).hasValue("123 Main Street, Hoffman Estates, IL");
  }
}