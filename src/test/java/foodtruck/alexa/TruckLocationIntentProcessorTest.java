package foodtruck.alexa;

import java.util.List;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import org.easymock.EasyMockSupport;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Location;
import foodtruck.model.Truck;
import foodtruck.model.TruckSchedule;
import foodtruck.model.TruckStop;
import foodtruck.truckstops.FoodTruckStopService;
import foodtruck.util.Clock;

import static com.google.common.truth.Truth.assertThat;
import static foodtruck.alexa.TruckLocationIntentProcessor.SLOT_TIME_OF_DAY;
import static foodtruck.alexa.TruckLocationIntentProcessor.SLOT_TRUCK;
import static org.easymock.EasyMock.expect;

/**
 * @author aviolette
 * @since 8/25/16
 */
public class TruckLocationIntentProcessorTest extends EasyMockSupport {
  private static final String CORNERFARMACY_NAME = "Corner Farmacy";
  private TruckLocationIntentProcessor processor;
  private FoodTruckStopService service;
  private Clock clock;
  private DateTime date;
  private TruckDAO truckDAO;
  private Truck cornerfarmacy;
  private ImmutableList<TruckStop> twoStops;
  private ImmutableList<TruckStop> fourStops;
  private ImmutableList<TruckStop> fiveStops;

  @Before
  public void before() {
    date = new DateTime(2016, 7, 15, 10, 0);
    this.clock = createMock(Clock.class);
    expect(clock.currentDay()).andStubReturn(date.toLocalDate());
    expect(clock.timeAt(17, 0)).andStubReturn(date.withTime(15, 0, 0, 0));
    expect(clock.timeAt(8, 0)).andStubReturn(date.withTime(8, 0, 0, 0));
    expect(clock.timeAt(12, 0)).andStubReturn(date.withTime(12, 0, 0, 0));
    this.service = createMock(FoodTruckStopService.class);
    this.truckDAO = createMock(TruckDAO.class);
    this.processor = new TruckLocationIntentProcessor(service, truckDAO, clock);
    Location wackerAndAdams = Location.builder().name("Wacker and Adams, Chicago, IL").lat(1).lng(2).build();
    Location sixhundred = Location.builder().name("600 West Chicago Avenue, Chicago, IL").lat(1).lng(2).build();
    Location clarkMonroe = Location.builder().name("Clark and Monroe, Chicago, IL").lat(1).lng(2).build();
    Location begyleBrewing = Location.builder().name("Begyle Brewing").lat(1).lng(2).build();
    Location urbanLegend = Location.builder().name("Urban Legend Brewery").lat(1).lng(2).build();
    this.cornerfarmacy = Truck.builder().name(CORNERFARMACY_NAME).id("cornerfarmacy").build();
    this.twoStops = ImmutableList.of(TruckStop.builder()
        .startTime(date.withTime(7, 0, 0, 0))
        .endTime(date.withTime(13, 30, 0, 0))
        .truck(cornerfarmacy)
        .location(clarkMonroe)
        .build(), TruckStop.builder()
        .startTime(date.withTime(17, 0, 0, 0))
        .endTime(date.withTime(21, 0, 0, 0))
        .truck(cornerfarmacy)
        .location(begyleBrewing)
        .build());
    this.fourStops = ImmutableList.of(TruckStop.builder()
        .startTime(date.withTime(6, 0, 0, 0))
        .endTime(date.withTime(6, 58, 0, 0))
        .truck(cornerfarmacy)
        .location(wackerAndAdams).build(), TruckStop.builder()
        .startTime(date.withTime(7, 30, 0, 0))
        .endTime(date.withTime(9, 30, 0, 0))
        .truck(cornerfarmacy)
        .location(clarkMonroe)
        .build(), TruckStop.builder()
        .startTime(date.withTime(11, 30, 0, 0))
        .endTime(date.withTime(13, 30, 0, 0))
        .truck(cornerfarmacy)
        .location(sixhundred)
        .build(), TruckStop.builder()
        .startTime(date.withTime(17, 0, 0, 0))
        .endTime(date.withTime(21, 0, 0, 0))
        .truck(cornerfarmacy)
        .location(urbanLegend)
        .build());
    List<TruckStop> fiveStops = Lists.newLinkedList(this.fourStops);
    fiveStops.add(TruckStop.builder()
        .startTime(date.withTime(17, 0, 0, 0))
        .endTime(date.withTime(21, 0, 0, 0))
        .truck(cornerfarmacy)
        .location(begyleBrewing)
        .build());
    this.fiveStops = ImmutableList.copyOf(fiveStops);
  }
  @Test
  public void process_NoTimeSpecified_HasOneStop() {
    expect(clock.now()).andStubReturn(date);
    runIt(null, twoStops.subList(0, 1), "Corner Farmacy is currently at Clark and Monroe");
  }

  @Test
  public void process_NoTimeSpecified_HasOneStopLater() {
    expect(clock.now()).andStubReturn(date);
    runIt(null, twoStops.subList(1, 2), "Corner Farmacy will be at Begyle Brewing at 5:00 PM");
  }

  @Test
  public void process_NoTimeSpecified_HasTwoStops() {
    expect(clock.now()).andStubReturn(date);
    runIt(null, twoStops, "Corner Farmacy is currently at Clark and Monroe and will be at Begyle Brewing at 5:00 PM");
  }

  @Test
  public void process_Today_HasFiveStops() {
    expect(clock.now()).andStubReturn(date);
    runIt("today", fiveStops,
        "Corner Farmacy will be at 600 West Chicago Avenue at 11:30 AM, Urban Legend Brewery at 5:00 PM, and Begyle Brewing at 5:00 PM");
  }

  @Test
  public void process_LunchInFuture_HasFiveStops() {
    expect(clock.now()).andStubReturn(date.withTime(10, 0, 0, 0));
    runIt("lunch", fiveStops, "Corner Farmacy will be at 600 West Chicago Avenue at 11:30 AM");
  }

  @Test
  public void process_LunchInPast_HasFiveStops() {
    expect(clock.now()).andStubReturn(date.withTime(15, 0, 0, 1));
    runIt("lunch", fiveStops, "Corner Farmacy was at 600 West Chicago Avenue for lunch");
  }

  @Test
  public void process_Now_HasTwoStops() {
    expect(clock.now()).andStubReturn(date);
    runIt("now", twoStops, "Corner Farmacy is currently at Clark and Monroe");
  }

  @Test
  public void process_Now_HasOneStops() {
    expect(clock.now()).andStubReturn(date);
    runIt("now", twoStops.subList(1, 2), "Corner Farmacy is not currently on the road");
  }

  @Test
  public void process_Later() {
    expect(clock.now()).andStubReturn(date);
    runIt("later", twoStops, "Corner Farmacy will be at Begyle Brewing at 5:00 PM");
  }

  @Test
  public void process_NoLater() {
    expect(clock.now()).andStubReturn(date);
    runIt("later", twoStops.subList(0, 1), "Corner Farmacy is not on the road for the remainder of the day");
  }

  @Test
  public void process_Dinner() {
    expect(clock.now()).andStubReturn(date);
    runIt("for dinner", twoStops, "Corner Farmacy will be at Begyle Brewing at 5:00 PM");
  }

  @Test
  public void process_Breakfast_Over() {
    expect(clock.now()).andStubReturn(date.withTime(14, 0, 1, 0));
    runIt("for breakfast", twoStops, "Corner Farmacy was at Clark and Monroe for breakfast");
  }

  @Test
  public void process_Breakfast_Over_MultipleStops() {
    expect(clock.now()).andStubReturn(date.withTime(14, 0, 1, 0));
    runIt("for breakfast", fourStops, "Corner Farmacy was at Clark and Monroe for breakfast");
  }

  @Test
  public void process_Lunch_Over() {
    expect(clock.now()).andStubReturn(date.withTime(14, 0, 1, 0));
    runIt("for lunch", twoStops, "Corner Farmacy was at Clark and Monroe for lunch");
  }

  private void runIt(String when, ImmutableList<TruckStop> stops, String expected) {
    Intent intent = Intent.builder()
        .withName(AlexaModule.WHERE_IS_TRUCK)
        .withSlots(
            ImmutableMap.of(SLOT_TRUCK, Slot.builder().withName(SLOT_TRUCK).withValue(CORNERFARMACY_NAME).build(),
                SLOT_TIME_OF_DAY, Slot.builder().withName(SLOT_TIME_OF_DAY).withValue(when).build()))
        .build();
    expect(truckDAO.findByName(CORNERFARMACY_NAME)).andReturn(cornerfarmacy);
    expect(service.findStopsForDay(cornerfarmacy.getId(), date.toLocalDate())).andReturn(
        new TruckSchedule(cornerfarmacy, date.toLocalDate(), stops));
    replayAll();
    SpeechletResponse response = processor.process(intent, null);
    assertThat(response.getCard().getTitle()).isEqualTo(CORNERFARMACY_NAME);
    assertThat(((PlainTextOutputSpeech) response.getOutputSpeech()).getText()).isEqualTo(expected);
    verifyAll();
  }
}