package foodtruck.socialmedia;

import com.google.common.collect.ImmutableList;

import org.easymock.EasyMockSupport;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import foodtruck.dao.DailyDataDAO;
import foodtruck.model.DailyData;
import foodtruck.model.Story;
import foodtruck.model.Truck;
import foodtruck.util.Clock;

import static org.easymock.EasyMock.expect;

/**
 * @author aviolette
 * @since 10/28/15
 */
public class SpecialUpdaterTest extends EasyMockSupport {
  private SpecialUpdater specialUpdater;
  private DailyDataDAO dailyDataDAO;
  private LocalDate localDate = new LocalDate(2015, 10, 10);

  @Before
  public void before() {
    Clock clock = createMock(Clock.class);
    expect(clock.currentDay()).andStubReturn(localDate);
    dailyDataDAO = createMock(DailyDataDAO.class);
    specialUpdater = new SpecialUpdater(dailyDataDAO, clock);
  }

  @Test
  public void ignoreNonVaultVan() {
    replayAll();
    specialUpdater.update(Truck.builder().id("hoosfoos").build(), ImmutableList.<Story>of());
    verifyAll();
  }

  @Test
  public void addCanalSpecial() {
    expect(dailyDataDAO.save(DailyData.builder()
        .locationId("Doughnut Vault @ Canal")
        .onDate(localDate)
        .addSpecial("raspberry old fashioned", false)
        .build()))
        .andReturn(1L);
    expect(dailyDataDAO.findByLocationAndDay("Doughnut Vault @ Canal", localDate)).andReturn(null);
    replayAll();
    specialUpdater.update(Truck.builder().id("thevaultvan").build(), ImmutableList.<Story>of(
        Story.builder().text("Good morning! #CanalVault has the raspberry old fashioned special today.").build()));
    verifyAll();
  }

  @Test
  public void addCanalSpecial2() {
    expect(dailyDataDAO.save(DailyData.builder()
        .locationId("Doughnut Vault @ Canal")
        .onDate(localDate)
        .addSpecial("apple cider old fashioned", false)
        .build()))
        .andReturn(1L);
    expect(dailyDataDAO.findByLocationAndDay("Doughnut Vault @ Canal", localDate)).andReturn(null);
    replayAll();
    specialUpdater.update(Truck.builder().id("thevaultvan").build(), ImmutableList.<Story>of(
        Story.builder().text("The special of today is Apple cider old fashioned! #CanalVault").build()));
    verifyAll();
  }

  @Test
  public void addBirthdayCake() {
    expect(dailyDataDAO.save(DailyData.builder()
        .locationId("Doughnut Vault @ Canal")
        .onDate(localDate)
        .addSpecial("birthday white cake", false)
        .build()))
        .andReturn(1L);
    expect(dailyDataDAO.findByLocationAndDay("Doughnut Vault @ Canal", localDate)).andReturn(null);
    replayAll();
    specialUpdater.update(Truck.builder().id("thevaultvan").build(), ImmutableList.<Story>of(
        Story.builder().text("Happy Monday! #CanalVault has the birthday white cake special today.").build()));
    verifyAll();
  }

  @Test
  public void addCanalWithPreexistingSpecial() {
    expect(dailyDataDAO.save(DailyData.builder()
        .locationId("Doughnut Vault @ Canal")
        .onDate(localDate)
        .addSpecial("apple cider old fashioned", false)
        .key(123L)
        .build()))
        .andReturn(123L);
    DailyData existing = DailyData.builder()
        .locationId("Doughnut Vault @ Canal")
        .onDate(localDate)
        .addSpecial("root cider old fashioned", false)
        .key(123L)
        .build();
    expect(dailyDataDAO.findByLocationAndDay("Doughnut Vault @ Canal", localDate)).andReturn(existing);
    replayAll();
    specialUpdater.update(Truck.builder().id("thevaultvan").build(), ImmutableList.<Story>of(
        Story.builder().text("The special of today is Apple cider old fashioned! #CanalVault").build()));
    verifyAll();
  }

  @Test
  public void soldOutWithSpecial() {
    expect(dailyDataDAO.save(DailyData.builder()
        .locationId("Doughnut Vault @ Canal")
        .onDate(localDate)
        .addSpecial("Apple Cider Old Fashioned", true)
        .key(123L)
        .build()))
        .andReturn(123L);
    DailyData existing = DailyData.builder()
        .locationId("Doughnut Vault @ Canal")
        .onDate(localDate)
        .addSpecial("Apple Cider Old Fashioned", false)
        .key(123L)
        .build();
    expect(dailyDataDAO.findByLocationAndDay("Doughnut Vault @ Canal", localDate)).andReturn(existing);
    replayAll();
    specialUpdater.update(Truck.builder().id("thevaultvan").build(), ImmutableList.<Story>of(
        Story.builder().text("#CanalVault is sold out.").build()));
    verifyAll();
  }
/*
  #FranklinVault has root beer old fashioned and the filled is strawberry jelly.
  Good morning, #CanalVault has the root beer old fashioned special today.
  #VaultVan is at Wabash & Jackson today and our special is root beer old fashioned.
  #CanalVault is sold out.
  Happy Monday! #CanalVault has pineapple old fashioned for special today.
  The special of today is Apple cider old fashioned! #FranklinVault
  Happy Friday! The special of today is Blueberry Old Fashioned at #FranklinVault
  Happy Thursday, #CanalVault has the sweet melon old fashioned special.
  #FranklinVault has birthday cake on special and the filled is raspberry jelly.
  Morning! #CanalVault has birthday cake special today!
  #VaultVan is open in 10 at Wabash and Jackson with our birthday cake special.
  #VaultVan is done for the day
  #FranklinVault has Butterscotch Crunch old fashioned and the filled is raspberry jelly.
  Happy Friday, #CanalVault has the praline pecan old fashioned special.
  Happy Saturday! The special of today is white chocolate cake and the jelly is blackberry at #FranklinVault
  #FranklinVault is open. Raspberry is the jelly and pumpkin cake with cream cheese glaze is the special
  The special of today is Salted Carmel Old Fashioned #FranklinVault happy Monday!
   */

  @Test
  public void addFranklinSpecial() {
    expect(dailyDataDAO.save(DailyData.builder()
        .locationId("Doughnut Vault @ Franklin")
        .onDate(localDate)
        .addSpecial("root beer old fashioned", false)
        .addSpecial("strawberry jelly", false)
        .build())).andReturn(1L);
    expect(dailyDataDAO.findByLocationAndDay("Doughnut Vault @ Franklin", localDate)).andReturn(null);
    replayAll();
    specialUpdater.update(Truck.builder().id("thevaultvan").build(), ImmutableList.<Story>of(
        Story.builder().text("#FranklinVault has root beer old fashioned and the filled is strawberry jelly.").build()));
    verifyAll();
  }

  @Test
  public void addVaultVan() {
    expect(dailyDataDAO.save(DailyData.builder()
        .truckId("thevaultvan")
        .onDate(localDate)
        .addSpecial("Pinã Colada Old-fashioned", false)
        .build())).andReturn(1L);
    expect(dailyDataDAO.findByTruckAndDay("thevaultvan", localDate)).andReturn(null);
    replayAll();
    specialUpdater.update(Truck.builder().id("thevaultvan").build(), ImmutableList.<Story>of(
        Story.builder().text("#VaultVan is at  LaSalle & Adams this morning with our piña colada special. Come on by.").build()));
    verifyAll();
  }

  @Test
  public void orangeCreamOldFashioned() {
    expect(dailyDataDAO.save(DailyData.builder()
        .truckId("thevaultvan")
        .onDate(localDate)
        .addSpecial("Orange Cream Old Fashioned with Chocolate Drizzle", false)
        .build())).andReturn(1L);
    expect(dailyDataDAO.findByTruckAndDay("thevaultvan", localDate)).andReturn(null);
    replayAll();
    specialUpdater.update(Truck.builder().id("thevaultvan").build(), ImmutableList.<Story>of(
        Story.builder().text("#VaultVan is at Oz Park with the orange cream old fashioned with chocolate drizzle").build()));
    verifyAll();
  }


  @Test
  public void strawberryDrizzle1() {
    expect(dailyDataDAO.save(DailyData.builder()
        .truckId("thevaultvan")
        .onDate(localDate)
        .addSpecial("Vanilla Old Fashioned with Strawberry Drizzle", false)
        .build())).andReturn(1L);
    expect(dailyDataDAO.findByTruckAndDay("thevaultvan", localDate)).andReturn(null);
    replayAll();
    specialUpdater.update(Truck.builder().id("thevaultvan").build(), ImmutableList.<Story>of(
        Story.builder().text("#VaultVan is at Wabash & Jackson!  The special is vanilla old fashioned with strawberry drizzle.").build()));
    verifyAll();
  }


  @Test
  public void keylimeCrunch() {
    expect(dailyDataDAO.save(DailyData.builder()
        .truckId("thevaultvan")
        .onDate(localDate)
        .addSpecial("Key lime crunch old fashioned", false)
        .build())).andReturn(1L);
    expect(dailyDataDAO.findByTruckAndDay("thevaultvan", localDate)).andReturn(null);
    replayAll();
    specialUpdater.update(Truck.builder().id("thevaultvan").build(), ImmutableList.<Story>of(
        Story.builder().text("Key lime crunch old fashioned is our special of the day. Come find #VaultVan at Lasalle and Adams this morning.").build()));
    verifyAll();
  }

  @Test
  public void creamCake() {
    expect(dailyDataDAO.save(DailyData.builder()
        .truckId("thevaultvan")
        .onDate(localDate)
        .addSpecial("Raspberry Cream Cake", false)
        .build())).andReturn(1L);
    expect(dailyDataDAO.findByTruckAndDay("thevaultvan", localDate)).andReturn(null);
    replayAll();
    specialUpdater.update(Truck.builder().id("thevaultvan").build(), ImmutableList.<Story>of(
        Story.builder().text("#VaultVan is at LaSalle & Adams with the raspberry cream cake").build()));
    verifyAll();
  }

  @Test
  public void doubleChocolateYellowCake() {
    expect(dailyDataDAO.save(DailyData.builder()
        .truckId("thevaultvan")
        .onDate(localDate)
        .addSpecial("Double Chocolate Yellow Cake", false)
        .build())).andReturn(1L);
    expect(dailyDataDAO.findByTruckAndDay("thevaultvan", localDate)).andReturn(null);
    replayAll();
    specialUpdater.update(Truck.builder().id("thevaultvan").build(), ImmutableList.<Story>of(
        Story.builder().text("#VaultVan is at Southport & Addison today, serving up hot Metropolis coffee with our double chocolate yellow cake special. Come say hi!\n").build()));
    verifyAll();
  }

  @Test
  public void mochaCoconutCrunch() {
    expect(dailyDataDAO.save(DailyData.builder()
        .truckId("thevaultvan")
        .onDate(localDate)
        .addSpecial("Mocha Coconut Crunch Cake", false)
        .build())).andReturn(1L);
    expect(dailyDataDAO.findByTruckAndDay("thevaultvan", localDate)).andReturn(null);
    replayAll();
    specialUpdater.update(Truck.builder().id("thevaultvan").build(), ImmutableList.<Story>of(
        Story.builder().text("Mocha coconut crunch cake is our special of the day at #VaultVan, posted at LaSalle & Adams for ya.").build()));
    verifyAll();
  }

  @Test @Ignore
  public void shortCake() {
    expect(dailyDataDAO.save(DailyData.builder()
        .truckId("thevaultvan")
        .onDate(localDate)
        .addSpecial("Strawberry Shortcake", false)
        .build())).andReturn(1L);
    expect(dailyDataDAO.findByTruckAndDay("thevaultvan", localDate)).andReturn(null);
    replayAll();
    specialUpdater.update(Truck.builder().id("thevaultvan").build(), ImmutableList.<Story>of(
        Story.builder().text("Happy Friday, #CanalVault has the strawberry shortcake with crumble.").build()));
    verifyAll();
  }
}