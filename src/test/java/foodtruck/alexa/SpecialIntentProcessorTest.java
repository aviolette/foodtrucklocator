package foodtruck.alexa;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.google.common.collect.ImmutableMap;

import org.easymock.EasyMockSupport;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import foodtruck.dao.DailyDataDAO;
import foodtruck.dao.TruckDAO;
import foodtruck.model.DailyData;
import foodtruck.model.Truck;
import foodtruck.util.Clock;

import static foodtruck.alexa.AlexaTestingUtils.assertSpeech;
import static foodtruck.alexa.SpecialIntentProcessor.TRUCK_SLOT;
import static org.easymock.EasyMock.expect;

/**
 * @author aviolette
 * @since 8/31/16
 */
public class SpecialIntentProcessorTest extends EasyMockSupport {

  private DailyDataDAO dailyDataDAO;
  private LocalDate date;
  private TruckDAO truckDAO;
  private Truck truck;
  private SpecialIntentProcessor processor;
  private Intent intent;

  @Before
  public void before() {
    truckDAO = createMock(TruckDAO.class);
    dailyDataDAO = createMock(DailyDataDAO.class);
    date = new LocalDate(2016, 7, 15);
    truck = Truck.builder()
        .name("Foobar")
        .id("foobar")
        .build();
    intent = Intent.builder()
        .withName(AlexaModule.DAILY_SPECIALS)
        .withSlots(ImmutableMap.of(TRUCK_SLOT, Slot.builder()
            .withName(TRUCK_SLOT)
            .withValue("Foobar")
            .build()))
        .build();
    Clock clock = createMock(Clock.class);
    expect(clock.currentDay()).andStubReturn(date);
    processor = new SpecialIntentProcessor(dailyDataDAO, truckDAO, clock);
  }

  @Test
  public void noTruck() {
    expect(truckDAO.findByNameOrAlias("Foobar")).andReturn(null);
    replayAll();
    SpeechletResponse response = processor.process(intent, null);
    assertSpeech(response.getOutputSpeech()).isEqualTo(TruckLocationIntentProcessor.TRUCK_NOT_FOUND);
    verifyAll();
  }


  @Test
  public void truckNull() {
    intent = Intent.builder()
        .withName(AlexaModule.DAILY_SPECIALS)
        .withSlots(ImmutableMap.of(TRUCK_SLOT, Slot.builder()
            .withName(TRUCK_SLOT)
            .build()))
        .build();
    replayAll();
    SpeechletResponse response = processor.process(intent, null);
    assertSpeech(response.getOutputSpeech()).isEqualTo(TruckLocationIntentProcessor.TRUCK_NOT_FOUND);
    verifyAll();
  }

  @Test
  public void noSpecials() {
    expect(truckDAO.findByNameOrAlias("Foobar")).andReturn(truck);
    expect(dailyDataDAO.findByTruckAndDay("foobar", date)).andReturn(null);
    replayAll();
    SpeechletResponse response = processor.process(intent, null);
    assertSpeech(response.getOutputSpeech()).isEqualTo("<speak>There are no specials for Foobar today</speak>");
    verifyAll();
  }

  @Test
  public void oneSpecial() {
    expect(truckDAO.findByNameOrAlias("Foobar")).andReturn(truck);
    DailyData dailyData = DailyData.builder()
        .addSpecial("Mexican Hot Chocolate Cake Donut", false)
        .build();
    expect(dailyDataDAO.findByTruckAndDay("foobar", date)).andReturn(dailyData);
    replayAll();
    SpeechletResponse response = processor.process(intent, null);
    assertSpeech(response.getOutputSpeech()).isEqualTo(
        "<speak>Foobar's special for today is Mexican Hot Chocolate Cake Donut.</speak>");
    verifyAll();
  }

  @Test
  public void oneSpecialSoldOut() {
    expect(truckDAO.findByNameOrAlias("Foobar")).andReturn(truck);
    DailyData dailyData = DailyData.builder()
        .addSpecial("Mexican Hot Chocolate Cake Donut", true)
        .build();
    expect(dailyDataDAO.findByTruckAndDay("foobar", date)).andReturn(dailyData);
    replayAll();
    SpeechletResponse response = processor.process(intent, null);
    assertSpeech(response.getOutputSpeech()).isEqualTo(
        "<speak>Foobar's special for today is Mexican Hot Chocolate Cake Donut but it appears to be sold out.</speak>");
    verifyAll();
  }



  @Test
  public void twoSpecials() {
    expect(truckDAO.findByNameOrAlias("Foobar")).andReturn(truck);
    DailyData dailyData = DailyData.builder()
        .addSpecial("Mexican Hot Chocolate Cake Donut", false)
        .addSpecial("Cherry Old Fashioned", false)
        .build();
    expect(dailyDataDAO.findByTruckAndDay("foobar", date)).andReturn(dailyData);
    replayAll();
    SpeechletResponse response = processor.process(intent, null);
    assertSpeech(response.getOutputSpeech()).isEqualTo(
        "<speak>Foobar's specials for today are Cherry Old Fashioned and Mexican Hot Chocolate Cake Donut</speak>");
    verifyAll();
  }
}