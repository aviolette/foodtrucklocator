package foodtruck.schedule;

import java.util.Optional;

import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import foodtruck.geolocation.GeoLocator;
import foodtruck.model.Location;

import static com.google.common.truth.Truth8.assertThat;
import static foodtruck.schedule.ModelTestHelper.clarkAndMonroe;
import static foodtruck.schedule.ModelTestHelper.truck1;
import static foodtruck.schedule.ModelTestHelper.unresolvedLocation;
import static foodtruck.schedule.ModelTestHelper.wackerAndAdams;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CalendarAddressExtractorTest {

  @Mock private AddressExtractor extractor;
  @Mock private GeoLocator geoLocator;
  private CalendarAddressExtractor cae;

  @Before
  public void before() {
    this.cae = new CalendarAddressExtractor(extractor, geoLocator);
  }

  @Test
  public void parseWholeString() {
    String loc = clarkAndMonroe().getName();
    when(geoLocator.broadSearch(loc)).thenReturn(Optional.of(clarkAndMonroe()));
    Optional<Location> actual = cae.parse(loc, truck1());
    verify(geoLocator).broadSearch(loc);
    assertThat(actual).hasValue(clarkAndMonroe());
  }

  @Test
  public void parseContainedAddressWholeAddressNotFound() {
    String loc = clarkAndMonroe().getName();
    when(geoLocator.broadSearch(loc)).thenReturn(Optional.empty());
    when(extractor.parse(loc, truck1())).thenReturn(ImmutableList.of("FOOBAR"));
    when(geoLocator.broadSearch("FOOBAR")).thenReturn(Optional.of(wackerAndAdams()));

    Optional<Location> actual = cae.parse(loc, truck1());

    assertThat(actual).hasValue(wackerAndAdams());
    verify(geoLocator).broadSearch(loc);
    verify(extractor).parse(loc, truck1());
    verify(geoLocator).broadSearch("FOOBAR");
  }

  @Test
  public void parseContainedAddressWholeAddressNotResolved() {
    String loc = clarkAndMonroe().getName();
    when(geoLocator.broadSearch(loc)).thenReturn(Optional.of(unresolvedLocation()));
    when(extractor.parse(loc, truck1())).thenReturn(ImmutableList.of("FOOBAR"));
    when(geoLocator.broadSearch("FOOBAR")).thenReturn(Optional.of(wackerAndAdams()));

    Optional<Location> actual = cae.parse(loc, truck1());

    assertThat(actual).hasValue(wackerAndAdams());
    verify(geoLocator).broadSearch(loc);
    verify(extractor).parse(loc, truck1());
    verify(geoLocator).broadSearch("FOOBAR");
  }

  @Test
  public void parseContainedAddressUnresolved() {
    String loc = clarkAndMonroe().getName();
    when(geoLocator.broadSearch(loc)).thenReturn(Optional.of(unresolvedLocation()));
    when(extractor.parse(loc, truck1())).thenReturn(ImmutableList.of("FOOBAR"));
    when(geoLocator.broadSearch("FOOBAR")).thenReturn(Optional.of(unresolvedLocation()));

    Optional<Location> actual = cae.parse(loc, truck1());

    assertThat(actual).isEmpty();
    verify(geoLocator).broadSearch(loc);
    verify(extractor).parse(loc, truck1());
    verify(geoLocator).broadSearch("FOOBAR");
  }
}