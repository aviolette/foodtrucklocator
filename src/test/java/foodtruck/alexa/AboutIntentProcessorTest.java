package foodtruck.alexa;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

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
import static foodtruck.alexa.AboutIntentProcessor.TRUCK_SLOT;
import static foodtruck.alexa.AlexaTestingUtils.assertSpeech;
import static org.easymock.EasyMock.expect;

/**
 * @author aviolette
 * @since 9/9/16
 */
public class AboutIntentProcessorTest extends EasyMockSupport {
  private static final String TRUCK_NAME = "Hoos Foos";
  private static final String TRUCK_ID = "snimnim";
  private TruckDAO truckDAO;
  private FoodTruckStopService service;
  private AboutIntentProcessor processor;
  private DateTime date;
  private Truck truck;
  private Location loc1;
  private Location loc3;
  private TruckStop stop1, stop2, stop3;

  @Before
  public void before() {
    date = new DateTime(2016, 7, 15, 10, 10);
    Clock clock = createMock(Clock.class);
    truckDAO = createMock(TruckDAO.class);
    service = createMock(FoodTruckStopService.class);
    truck = Truck.builder()
        .name(TRUCK_NAME)
        .description("This truck sells tacos.")
        .previewIcon("http://hoos.com/foos.png")
        .id(TRUCK_ID)
        .build();
    loc1 = Location.builder()
        .name("Location A")
        .build();
    Location loc2 = Location.builder()
        .name("Location B, Chicago, IL")
        .build();
    loc3 = Location.builder()
        .name("Location C")
        .build();
    TruckStop.Builder builder = TruckStop.builder()
        .truck(truck);
    stop1 = builder.startTime(date.minusHours(1))
        .endTime(date.plusHours(1))
        .location(loc1)
        .build();
    stop2 = TruckStop.builder()
        .startTime(date.plusHours(1))
        .endTime(date.plusHours(2))
        .location(loc2)
        .build();
    stop3 = TruckStop.builder()
        .startTime(date.minusHours(2))
        .endTime(date.minusHours(1))
        .location(loc3)
        .build();
    expect(clock.now()).andStubReturn(date);
    expect(clock.currentDay()).andStubReturn(date.toLocalDate());
    processor = new AboutIntentProcessor(truckDAO, clock, service);
  }

  private Intent intent(String truckSlot) {
    return Intent.builder()
        .withName(AlexaModule.ABOUT_TRUCK)
        .withSlots(ImmutableMap.of(TRUCK_SLOT, Slot.builder()
            .withName(TRUCK_SLOT)
            .withValue(truckSlot)
            .build()))
        .build();
  }

  @Test
  public void processNoTruckSpecified() throws Exception {
    replayAll();
    SpeechletResponse response = processor.process(intent(null), null);
    assertSpeech(response.getOutputSpeech()).isEqualTo(TruckLocationIntentProcessor.TRUCK_NOT_FOUND);
    assertSpeech(response.getReprompt()
        .getOutputSpeech()).isEqualTo(TruckLocationIntentProcessor.TRUCK_NOT_FOUND_REPROMPT);
    verifyAll();
  }

  @Test
  public void processIncorrectTruckSpecified() throws Exception {
    expect(truckDAO.findByNameOrAlias("unknown")).andReturn(null);
    replayAll();
    SpeechletResponse response = processor.process(intent("unknown"), null);
    assertSpeech(response.getOutputSpeech()).isEqualTo(TruckLocationIntentProcessor.TRUCK_NOT_FOUND);
    assertSpeech(response.getReprompt()
        .getOutputSpeech()).isEqualTo(TruckLocationIntentProcessor.TRUCK_NOT_FOUND_REPROMPT);
    verifyAll();
  }

  @Test
  public void processHasFutureStops() throws Exception {
    expect(truckDAO.findByNameOrAlias(TRUCK_NAME)).andReturn(truck);
    ImmutableList<TruckStop> stops = ImmutableList.of(TruckStop.builder()
        .startTime(date.plusHours(1))
        .endTime(date.plusHours(2))
        .location(loc1)
        .truck(truck)
        .build());
    TruckSchedule schedule = new TruckSchedule(truck, date.toLocalDate(), stops);
    expect(service.findStopsForDay(TRUCK_ID, date.toLocalDate())).andReturn(schedule);
    replayAll();
    SpeechletResponse response = processor.process(intent(TRUCK_NAME), null);
    assertSpeech(response.getOutputSpeech()).isEqualTo(
        "<speak>This truck sells tacos.<break time=\"0.3s\"/> Hoos Foos will be at Location A at 11:10 AM.</speak>");
    assertThat(response.getReprompt()).isNull();
    assertThat(response.getCard()).isNotNull();
    ImageCard imageCard = (ImageCard) response.getCard();
    assertThat(imageCard.getText()).isEqualTo("This truck sells tacos.\nHoos Foos will be at Location A at 11:10 AM.");
    ImageSet imageSet = imageCard.getImage();
    assertThat(imageSet.getSmallImageUrl()).isEqualTo("https://hoos.com/foos.png");
    verifyAll();
  }

  @Test
  public void processHasCurrentAndFutureStops() throws Exception {
    expect(truckDAO.findByNameOrAlias(TRUCK_NAME)).andReturn(truck);
    TruckSchedule schedule = new TruckSchedule(truck, date.toLocalDate(), ImmutableList.of(stop1, stop2));
    expect(service.findStopsForDay(TRUCK_ID, date.toLocalDate())).andReturn(schedule);
    replayAll();
    SpeechletResponse response = processor.process(intent(TRUCK_NAME), null);
    assertSpeech(response.getOutputSpeech()).isEqualTo(
        "<speak>This truck sells tacos.<break time=\"0.3s\"/> Hoos Foos is currently at Location A. Hoos Foos will be at Location B at 11:10 AM.</speak>");
    assertThat(response.getReprompt()).isNull();
    assertThat(response.getCard()).isNotNull();
    ImageCard imageCard = (ImageCard) response.getCard();
    assertThat(imageCard.getText()).isEqualTo(
        "This truck sells tacos.\nHoos Foos is currently at Location A. Hoos Foos will be at Location B at 11:10 AM.");
    ImageSet imageSet = imageCard.getImage();
    assertThat(imageSet.getSmallImageUrl()).isEqualTo("https://hoos.com/foos.png");
    verifyAll();
  }

  @Test
  public void processHasCurrentStops() throws Exception {
    expect(truckDAO.findByNameOrAlias(TRUCK_NAME)).andReturn(truck);
    TruckSchedule schedule = new TruckSchedule(truck, date.toLocalDate(), ImmutableList.of(stop1));
    expect(service.findStopsForDay(TRUCK_ID, date.toLocalDate())).andReturn(schedule);
    replayAll();
    SpeechletResponse response = processor.process(intent(TRUCK_NAME), null);
    assertSpeech(response.getOutputSpeech()).isEqualTo(
        "<speak>This truck sells tacos.<break time=\"0.3s\"/> Hoos Foos is currently at Location A.</speak>");
    assertThat(response.getReprompt()).isNull();
    assertThat(response.getCard()).isNotNull();
    ImageCard imageCard = (ImageCard) response.getCard();
    assertThat(imageCard.getText()).isEqualTo("This truck sells tacos.\nHoos Foos is currently at Location A.");
    ImageSet imageSet = imageCard.getImage();
    assertThat(imageSet.getSmallImageUrl()).isEqualTo("https://hoos.com/foos.png");
    verifyAll();
  }

  @Test
  public void processHasPastStopsToday() throws Exception {
    expect(truckDAO.findByNameOrAlias(TRUCK_NAME)).andReturn(truck);
    TruckSchedule schedule = new TruckSchedule(truck, date.toLocalDate(), ImmutableList.of(stop3));
    expect(service.findStopsForDay(TRUCK_ID, date.toLocalDate())).andReturn(schedule);
    replayAll();
    SpeechletResponse response = processor.process(intent(TRUCK_NAME), null);
    assertSpeech(response.getOutputSpeech()).isEqualTo(
        "<speak>This truck sells tacos.<break time=\"0.3s\"/> Hoos Foos was last seen today at Location C.</speak>");
    assertThat(response.getReprompt()).isNull();
    assertThat(response.getCard()).isNotNull();
    ImageCard imageCard = (ImageCard) response.getCard();
    assertThat(imageCard.getText()).isEqualTo("This truck sells tacos.\nHoos Foos was last seen today at Location C.");
    ImageSet imageSet = imageCard.getImage();
    assertThat(imageSet.getSmallImageUrl()).isEqualTo("https://hoos.com/foos.png");
    verifyAll();
  }

  @Test
  public void processHasPastStopsUsingStats() throws Exception {
    Truck.Stats stats = Truck.Stats.builder()
        .firstSeen(new DateTime(2013, 11, 15, 5, 0))
        .lastSeen(new DateTime(2016, 6, 13, 15, 15))
        .whereLastSeen(loc3)
        .build();
    truck = Truck.builder(truck)
        .stats(stats)
        .build();
    expect(truckDAO.findByNameOrAlias(TRUCK_NAME)).andReturn(truck);
    TruckSchedule schedule = new TruckSchedule(truck, date.toLocalDate(), ImmutableList.<TruckStop>of());
    expect(service.findStopsForDay(TRUCK_ID, date.toLocalDate())).andReturn(schedule);
    replayAll();
    SpeechletResponse response = processor.process(intent(TRUCK_NAME), null);
    assertSpeech(response.getOutputSpeech()).isEqualTo(
        "<speak>This truck sells tacos.<break time=\"0.3s\"/> Hoos Foos was last seen 31 days ago at Location C.</speak>");
    assertThat(response.getReprompt()).isNull();
    assertThat(response.getCard()).isNotNull();
    ImageCard imageCard = (ImageCard) response.getCard();
    assertThat(imageCard.getText()).isEqualTo(
        "This truck sells tacos.\nHoos Foos was last seen 31 days ago at Location C.");
    ImageSet imageSet = imageCard.getImage();
    assertThat(imageSet.getSmallImageUrl()).isEqualTo("https://hoos.com/foos.png");
    verifyAll();
  }

  @Test
  public void processHasNeverBeenOnRoad() throws Exception {
    Truck.Stats stats = Truck.Stats.builder()
        .build();
    truck = Truck.builder(truck)
        .stats(stats)
        .build();
    expect(truckDAO.findByNameOrAlias(TRUCK_NAME)).andReturn(truck);
    TruckSchedule schedule = new TruckSchedule(truck, date.toLocalDate(), ImmutableList.<TruckStop>of());
    expect(service.findStopsForDay(TRUCK_ID, date.toLocalDate())).andReturn(schedule);
    replayAll();
    SpeechletResponse response = processor.process(intent(TRUCK_NAME), null);
    assertSpeech(response.getOutputSpeech()).isEqualTo(
        "<speak>This truck sells tacos.<break time=\"0.3s\"/> Hoos Foos has never been seen on the road.</speak>");
    assertThat(response.getReprompt()).isNull();
    assertThat(response.getCard()).isNotNull();
    ImageCard imageCard = (ImageCard) response.getCard();
    assertThat(imageCard.getText()).isEqualTo("This truck sells tacos.\nHoos Foos has never been seen on the road.");
    ImageSet imageSet = imageCard.getImage();
    assertThat(imageSet.getSmallImageUrl()).isEqualTo("https://hoos.com/foos.png");
    verifyAll();
  }
}