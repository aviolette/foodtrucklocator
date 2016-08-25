package foodtruck.alexa;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.easymock.EasyMockSupport;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Before;
import org.junit.Test;

import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.Location;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.util.Clock;

import static com.google.common.truth.Truth.assertThat;
import static foodtruck.alexa.LocationIntentProcessor.SLOT_LOCATION;
import static foodtruck.alexa.LocationIntentProcessor.SLOT_WHEN;
import static org.easymock.EasyMock.expect;

/**
 * @author aviolette
 * @since 8/25/16
 */
public class LocationIntentProcessorTest extends EasyMockSupport {

  private GeoLocator locator;
  private Clock clock;
  private FoodTruckStopService service;
  private LocationIntentProcessor processor;
  private DateTime date;
  private Location location;

  @Before
  public void before() {
    locator = createMock(GeoLocator.class);
    service = createMock(FoodTruckStopService.class);
    clock = createMock(Clock.class);
    date = new DateTime(2016, 7, 15, 10, 10);
    location = Location.builder().name("Clark and Monroe").lat(12).lng(13).build();
    expect(clock.now()).andStubReturn(date);
    expect(clock.currentDay()).andStubReturn(date.toLocalDate());
    DateTimeFormatter formatter = DateTimeFormat.forPattern("YYYY-MM-dd").withZone(date.getZone());
    processor = new LocationIntentProcessor(locator, service, clock, formatter);
  }

  @Test
  public void procesWithNoDateNoTrucks() throws Exception {
    Intent intent = Intent.builder()
        .withName(AlexaModule.GET_FOOD_TRUCKS_AT_LOCATION)
        .withSlots(
            ImmutableMap.of(SLOT_LOCATION, Slot.builder().withName(SLOT_LOCATION).withValue("Clark and Monroe").build(),
                SLOT_WHEN, Slot.builder().withName(SLOT_WHEN).build()))
        .build();
    expect(locator.locate("Clark and Monroe", GeolocationGranularity.NARROW)).andReturn(location);
    expect(service.findStopsNearALocation(location, date.toLocalDate())).andReturn(ImmutableList.<TruckStop>of());
    replayAll();
    SpeechletResponse response = processor.process(intent, null);
    assertThat(response.getCard().getTitle()).isEqualTo("Food Trucks at Clark and Monroe");
    assertThat(((PlainTextOutputSpeech) response.getOutputSpeech()).getText()).isEqualTo(
        "There are no trucks at Clark and Monroe today");
    verifyAll();
  }

  @Test
  public void procesWithNoDateOneTruck() throws Exception {
    Intent intent = Intent.builder()
        .withName(AlexaModule.GET_FOOD_TRUCKS_AT_LOCATION)
        .withSlots(
            ImmutableMap.of(SLOT_LOCATION, Slot.builder().withName(SLOT_LOCATION).withValue("Clark and Monroe").build(),
                SLOT_WHEN, Slot.builder().withName(SLOT_WHEN).build()))
        .build();
    expect(locator.locate("Clark and Monroe", GeolocationGranularity.NARROW)).andReturn(location);
    expect(service.findStopsNearALocation(location, date.toLocalDate())).andReturn(
        ImmutableList.of(TruckStop.builder().truck(Truck.builder().name("The Fat Pickle").build()).build()));
    replayAll();
    SpeechletResponse response = processor.process(intent, null);
    assertThat(response.getCard().getTitle()).isEqualTo("Food Trucks at Clark and Monroe");
    assertThat(((PlainTextOutputSpeech) response.getOutputSpeech()).getText()).isEqualTo(
        "The Fat Pickle is the only food truck at Clark and Monroe today");
    verifyAll();
  }

  @Test
  public void processWithNoDateTwoTrucks() throws Exception {
    Intent intent = Intent.builder()
        .withName(AlexaModule.GET_FOOD_TRUCKS_AT_LOCATION)
        .withSlots(
            ImmutableMap.of(SLOT_LOCATION, Slot.builder().withName(SLOT_LOCATION).withValue("Clark and Monroe").build(),
                SLOT_WHEN, Slot.builder().withName(SLOT_WHEN).build()))
        .build();
    expect(locator.locate("Clark and Monroe", GeolocationGranularity.NARROW)).andReturn(location);
    expect(service.findStopsNearALocation(location, date.toLocalDate())).andReturn(
        ImmutableList.of(TruckStop.builder().truck(Truck.builder().name("Beavers Donuts").build()).build(),
            TruckStop.builder().truck(Truck.builder().name("The Fat Pickle").build()).build()));
    replayAll();
    SpeechletResponse response = processor.process(intent, null);
    assertThat(response.getCard().getTitle()).isEqualTo("Food Trucks at Clark and Monroe");
    assertThat(((PlainTextOutputSpeech) response.getOutputSpeech()).getText()).isEqualTo(
        "Beavers Donuts and The Fat Pickle are at Clark and Monroe today");
    verifyAll();
  }

  @Test
  public void processWithNoDateThreeTrucks() throws Exception {
    Intent intent = Intent.builder()
        .withName(AlexaModule.GET_FOOD_TRUCKS_AT_LOCATION)
        .withSlots(
            ImmutableMap.of(SLOT_LOCATION, Slot.builder().withName(SLOT_LOCATION).withValue("Clark and Monroe").build(),
                SLOT_WHEN, Slot.builder().withName(SLOT_WHEN).build()))
        .build();
    expect(locator.locate("Clark and Monroe", GeolocationGranularity.NARROW)).andReturn(location);
    expect(service.findStopsNearALocation(location, date.toLocalDate())).andReturn(
        ImmutableList.of(TruckStop.builder().truck(Truck.builder().name("Beavers Donuts").build()).build(),
            TruckStop.builder().truck(Truck.builder().name("Chicagos Finest").build()).build(),
            TruckStop.builder().truck(Truck.builder().name("The Fat Pickle").build()).build()));
    replayAll();
    SpeechletResponse response = processor.process(intent, null);
    assertThat(response.getCard().getTitle()).isEqualTo("Food Trucks at Clark and Monroe");
    assertThat(((PlainTextOutputSpeech) response.getOutputSpeech()).getText()).isEqualTo(
        "There are 3 trucks at Clark and Monroe today: Beavers Donuts, Chicagos Finest, and The Fat Pickle");
    verifyAll();
  }

  @Test
  public void processOneToday() throws Exception {
    Intent intent = Intent.builder()
        .withName(AlexaModule.GET_FOOD_TRUCKS_AT_LOCATION)
        .withSlots(
            ImmutableMap.of(SLOT_LOCATION, Slot.builder().withName(SLOT_LOCATION).withValue("Clark and Monroe").build(),
                SLOT_WHEN, Slot.builder().withName(SLOT_WHEN).withValue("2016-07-15").build()))
        .build();
    expect(locator.locate("Clark and Monroe", GeolocationGranularity.NARROW)).andReturn(location);
    expect(service.findStopsNearALocation(location, date.toLocalDate())).andReturn(
        ImmutableList.of(TruckStop.builder().truck(Truck.builder().name("The Fat Pickle").build()).build()));
    replayAll();
    SpeechletResponse response = processor.process(intent, null);
    assertThat(response.getCard().getTitle()).isEqualTo("Food Trucks at Clark and Monroe");
    assertThat(((PlainTextOutputSpeech) response.getOutputSpeech()).getText()).isEqualTo(
        "The Fat Pickle is the only food truck at Clark and Monroe today");
    verifyAll();
  }

  @Test
  public void processTwoToday() throws Exception {
    Intent intent = Intent.builder()
        .withName(AlexaModule.GET_FOOD_TRUCKS_AT_LOCATION)
        .withSlots(
            ImmutableMap.of(SLOT_LOCATION, Slot.builder().withName(SLOT_LOCATION).withValue("Clark and Monroe").build(),
                SLOT_WHEN, Slot.builder().withName(SLOT_WHEN).withValue("2016-07-15").build()))
        .build();
    expect(locator.locate("Clark and Monroe", GeolocationGranularity.NARROW)).andReturn(location);
    expect(service.findStopsNearALocation(location, date.toLocalDate())).andReturn(
        ImmutableList.of(TruckStop.builder().truck(Truck.builder().name("Beavers Donuts").build()).build(),
            TruckStop.builder().truck(Truck.builder().name("The Fat Pickle").build()).build()));
    replayAll();
    SpeechletResponse response = processor.process(intent, null);
    assertThat(response.getCard().getTitle()).isEqualTo("Food Trucks at Clark and Monroe");
    assertThat(((PlainTextOutputSpeech) response.getOutputSpeech()).getText()).isEqualTo(
        "Beavers Donuts and The Fat Pickle are at Clark and Monroe today");
    verifyAll();
  }

  @Test
  public void processThreeToday() throws Exception {
    Intent intent = Intent.builder()
        .withName(AlexaModule.GET_FOOD_TRUCKS_AT_LOCATION)
        .withSlots(
            ImmutableMap.of(SLOT_LOCATION, Slot.builder().withName(SLOT_LOCATION).withValue("Clark and Monroe").build(),
                SLOT_WHEN, Slot.builder().withName(SLOT_WHEN).withValue("2016-07-15").build()))
        .build();
    expect(locator.locate("Clark and Monroe", GeolocationGranularity.NARROW)).andReturn(location);
    expect(service.findStopsNearALocation(location, date.toLocalDate())).andReturn(
        ImmutableList.of(TruckStop.builder().truck(Truck.builder().name("Beavers Donuts").build()).build(),
            TruckStop.builder().truck(Truck.builder().name("Chicagos Finest").build()).build(),
            TruckStop.builder().truck(Truck.builder().name("The Fat Pickle").build()).build()));
    replayAll();
    SpeechletResponse response = processor.process(intent, null);
    assertThat(response.getCard().getTitle()).isEqualTo("Food Trucks at Clark and Monroe");
    assertThat(((PlainTextOutputSpeech) response.getOutputSpeech()).getText()).isEqualTo(
        "There are 3 trucks at Clark and Monroe today: Beavers Donuts, Chicagos Finest, and The Fat Pickle");
    verifyAll();
  }

  @Test
  public void processOneTomorrow() throws Exception {
    Intent intent = Intent.builder()
        .withName(AlexaModule.GET_FOOD_TRUCKS_AT_LOCATION)
        .withSlots(
            ImmutableMap.of(SLOT_LOCATION, Slot.builder().withName(SLOT_LOCATION).withValue("Clark and Monroe").build(),
                SLOT_WHEN, Slot.builder().withName(SLOT_WHEN).withValue("2016-07-16").build()))
        .build();
    date = date.withDayOfMonth(16);
    expect(locator.locate("Clark and Monroe", GeolocationGranularity.NARROW)).andReturn(location);
    expect(service.findStopsNearALocation(location, date.toLocalDate())).andReturn(
        ImmutableList.of(TruckStop.builder().truck(Truck.builder().name("The Fat Pickle").build()).build()));
    replayAll();
    SpeechletResponse response = processor.process(intent, null);
    assertThat(response.getCard().getTitle()).isEqualTo("Food Trucks at Clark and Monroe");
    assertThat(((PlainTextOutputSpeech) response.getOutputSpeech()).getText()).isEqualTo(
        "The Fat Pickle is the only food truck scheduled to be at Clark and Monroe tomorrow");
    verifyAll();
  }

  @Test
  public void processTwoTomorrow() throws Exception {
    Intent intent = Intent.builder()
        .withName(AlexaModule.GET_FOOD_TRUCKS_AT_LOCATION)
        .withSlots(
            ImmutableMap.of(SLOT_LOCATION, Slot.builder().withName(SLOT_LOCATION).withValue("Clark and Monroe").build(),
                SLOT_WHEN, Slot.builder().withName(SLOT_WHEN).withValue("2016-07-16").build()))
        .build();
    date = date.withDayOfMonth(16);
    expect(locator.locate("Clark and Monroe", GeolocationGranularity.NARROW)).andReturn(location);
    expect(service.findStopsNearALocation(location, date.toLocalDate())).andReturn(
        ImmutableList.of(TruckStop.builder().truck(Truck.builder().name("Beavers Donuts").build()).build(),
            TruckStop.builder().truck(Truck.builder().name("The Fat Pickle").build()).build()));
    replayAll();
    SpeechletResponse response = processor.process(intent, null);
    assertThat(response.getCard().getTitle()).isEqualTo("Food Trucks at Clark and Monroe");
    assertThat(((PlainTextOutputSpeech) response.getOutputSpeech()).getText()).isEqualTo(
        "Beavers Donuts and The Fat Pickle are scheduled to be at Clark and Monroe tomorrow");
    verifyAll();
  }

  @Test
  public void processThreeTomorrow() throws Exception {
    Intent intent = Intent.builder()
        .withName(AlexaModule.GET_FOOD_TRUCKS_AT_LOCATION)
        .withSlots(
            ImmutableMap.of(SLOT_LOCATION, Slot.builder().withName(SLOT_LOCATION).withValue("Clark and Monroe").build(),
                SLOT_WHEN, Slot.builder().withName(SLOT_WHEN).withValue("2016-07-16").build()))
        .build();
    date = date.withDayOfMonth(16);
    expect(locator.locate("Clark and Monroe", GeolocationGranularity.NARROW)).andReturn(location);
    expect(service.findStopsNearALocation(location, date.toLocalDate())).andReturn(
        ImmutableList.of(TruckStop.builder().truck(Truck.builder().name("Beavers Donuts").build()).build(),
            TruckStop.builder().truck(Truck.builder().name("Chicagos Finest").build()).build(),
            TruckStop.builder().truck(Truck.builder().name("The Fat Pickle").build()).build()));
    replayAll();
    SpeechletResponse response = processor.process(intent, null);
    assertThat(response.getCard().getTitle()).isEqualTo("Food Trucks at Clark and Monroe");
    assertThat(((PlainTextOutputSpeech) response.getOutputSpeech()).getText()).isEqualTo(
        "There are 3 trucks scheduled to be at Clark and Monroe tomorrow: Beavers Donuts, Chicagos Finest, and The Fat Pickle");
    verifyAll();
  }
}