package foodtruck.schedule;

import com.google.common.collect.ImmutableList;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import foodtruck.dao.DailyDataDAO;
import foodtruck.model.DailyData;
import foodtruck.model.Story;
import foodtruck.model.Truck;
import foodtruck.time.Clock;

/**
 * @author aviolette
 * @since 10/28/15
 */
@RunWith(MockitoJUnitRunner.class)
public class SpecialUpdaterTest extends Mockito {
  private static final String THEVAULTVANID = "thevaultvan";
  private static final Truck THEVAULTVAN = Truck.builder().id(THEVAULTVANID).build();
  private SpecialUpdater specialUpdater;
  @Mock private DailyDataDAO dailyDataDAO;
  private LocalDate localDate = new LocalDate(2015, 10, 10);

  @Before
  public void before() {
    Clock clock = mock(Clock.class);
    when(clock.currentDay()).thenReturn(localDate);
    specialUpdater = new SpecialUpdater(dailyDataDAO, clock);
  }

  @Test
  public void ignoreNonVaultVan() {
    specialUpdater.update(Truck.builder().id("hoosfoos").build(), ImmutableList.<Story>of());
  }

  @Test
  public void addCanalSpecial() {
    when(dailyDataDAO.save(DailyData.builder()
        .locationId("Doughnut Vault @ Canal")
        .onDate(localDate)
        .addSpecial("raspberry old fashioned", false)
        .build()))
        .thenReturn(1L);
    when(dailyDataDAO.findByLocationAndDay("Doughnut Vault @ Canal", localDate)).thenReturn(null);
    
    specialUpdater.update(THEVAULTVAN, ImmutableList.of(
        Story.builder().text("Good morning! #CanalVault has the raspberry old fashioned special today.").build()));
    
  }

  @Test
  public void addCanalSpecial2() {
    when(dailyDataDAO.save(DailyData.builder()
        .locationId("Doughnut Vault @ Canal")
        .onDate(localDate)
        .addSpecial("apple cider old fashioned", false)
        .build()))
        .thenReturn(1L);
    when(dailyDataDAO.findByLocationAndDay("Doughnut Vault @ Canal", localDate)).thenReturn(null);
    
    specialUpdater.update(THEVAULTVAN, ImmutableList.of(
        Story.builder().text("The special of today is Apple cider old fashioned! #CanalVault").build()));
    
  }

  @Test
  public void addBirthdayCake() {
    when(dailyDataDAO.save(DailyData.builder()
        .locationId("Doughnut Vault @ Canal")
        .onDate(localDate)
        .addSpecial("birthday white cake", false)
        .build()))
        .thenReturn(1L);
    when(dailyDataDAO.findByLocationAndDay("Doughnut Vault @ Canal", localDate)).thenReturn(null);
    
    specialUpdater.update(THEVAULTVAN, ImmutableList.of(
        Story.builder().text("Happy Monday! #CanalVault has the birthday white cake special today.").build()));
    
  }

  @Test
  public void pumpkinCake() {
    when(dailyDataDAO.save(DailyData.builder()
        .truckId(THEVAULTVANID)
        .onDate(localDate)
        .addSpecial("Pumpkin Cake", false)
        .build())).thenReturn(1L);
    when(dailyDataDAO.findByTruckAndDay(THEVAULTVANID, localDate)).thenReturn(null);
    
    specialUpdater.update(THEVAULTVAN, ImmutableList.of(Story.builder()
        .text(
            "#VaultVan has coffee caramel old fashioned and pumpkin cake as our specials today. Find us on Lasalle and Adams.")
        .build()));
    

  }


  @Test
  public void addCanalWithPreexistingSpecial() {
    when(dailyDataDAO.save(DailyData.builder()
        .locationId("Doughnut Vault @ Canal")
        .onDate(localDate)
        .addSpecial("apple cider old fashioned", false)
        .key(123L)
        .build()))
        .thenReturn(123L);
    DailyData existing = DailyData.builder()
        .locationId("Doughnut Vault @ Canal")
        .onDate(localDate)
        .addSpecial("root cider old fashioned", false)
        .key(123L)
        .build();
    when(dailyDataDAO.findByLocationAndDay("Doughnut Vault @ Canal", localDate)).thenReturn(existing);
    
    specialUpdater.update(THEVAULTVAN, ImmutableList.of(
        Story.builder().text("The special of today is Apple cider old fashioned! #CanalVault").build()));
    
  }

  @Test
  public void soldOutWithSpecial() {
    when(dailyDataDAO.save(DailyData.builder()
        .locationId("Doughnut Vault @ Canal")
        .onDate(localDate)
        .addSpecial("Apple Cider Old Fashioned", true)
        .key(123L)
        .build()))
        .thenReturn(123L);
    DailyData existing = DailyData.builder()
        .locationId("Doughnut Vault @ Canal")
        .onDate(localDate)
        .addSpecial("Apple Cider Old Fashioned", false)
        .key(123L)
        .build();
    when(dailyDataDAO.findByLocationAndDay("Doughnut Vault @ Canal", localDate)).thenReturn(existing);
    
    specialUpdater.update(THEVAULTVAN, ImmutableList.of(Story.builder().text("#CanalVault is sold out.").build()));
    
  }

  @Test
  public void addFranklinSpecial() {
    when(dailyDataDAO.save(DailyData.builder()
        .locationId("Doughnut Vault @ Franklin")
        .onDate(localDate)
        .addSpecial("root beer old fashioned", false)
        .addSpecial("strawberry jelly", false)
        .build())).thenReturn(1L);
    when(dailyDataDAO.findByLocationAndDay("Doughnut Vault @ Franklin", localDate)).thenReturn(null);
    
    specialUpdater.update(THEVAULTVAN, ImmutableList.of(
        Story.builder().text("#FranklinVault has root beer old fashioned and the filled is strawberry jelly.").build()));
    
  }

  @Test
  public void addDulcheDeLeche() {
    when(dailyDataDAO.save(DailyData.builder()
        .truckId(THEVAULTVANID)
        .onDate(localDate)
        .addSpecial("Dulce De Leche Cake", false)
        .build())).thenReturn(1L);
    when(dailyDataDAO.findByTruckAndDay(THEVAULTVANID, localDate)).thenReturn(null);
    
    specialUpdater.update(THEVAULTVAN, ImmutableList.of(Story.builder()
        .text("#VaultVan is at Southport and Addison. Today's special is dulce de leche cake.")
        .build()));
    
  }

  public void addVaultVan() {
    when(dailyDataDAO.save(DailyData.builder()
        .truckId(THEVAULTVANID)
        .onDate(localDate)
        .addSpecial("Pinã Colada Old-fashioned", false)
        .build())).thenReturn(1L);
    when(dailyDataDAO.findByTruckAndDay(THEVAULTVANID, localDate)).thenReturn(null);
    
    specialUpdater.update(THEVAULTVAN, ImmutableList.of(Story.builder()
        .text("#VaultVan is at  LaSalle & Adams this morning with our piña colada special. Come on by.")
        .build()));
    
  }

  @Test
  public void orangeCreamOldFashioned() {
    when(dailyDataDAO.save(DailyData.builder()
        .truckId(THEVAULTVANID)
        .onDate(localDate)
        .addSpecial("Orange Cream Old Fashioned with Chocolate Drizzle", false)
        .build())).thenReturn(1L);
    when(dailyDataDAO.findByTruckAndDay(THEVAULTVANID, localDate)).thenReturn(null);
    
    specialUpdater.update(THEVAULTVAN, ImmutableList.of(
        Story.builder().text("#VaultVan is at Oz Park with the orange cream old fashioned with chocolate drizzle").build()));
    
  }


  @Test
  public void strawberryDrizzle1() {
    when(dailyDataDAO.save(DailyData.builder()
        .truckId(THEVAULTVANID)
        .onDate(localDate)
        .addSpecial("Vanilla Old Fashioned with Strawberry Drizzle", false)
        .build())).thenReturn(1L);
    when(dailyDataDAO.findByTruckAndDay(THEVAULTVANID, localDate)).thenReturn(null);
    
    specialUpdater.update(THEVAULTVAN, ImmutableList.<Story>of(
        Story.builder().text("#VaultVan is at Wabash & Jackson!  The special is vanilla old fashioned with strawberry drizzle.").build()));
    
  }

// #VaultVan has raspberry old fashioned with crumble at LaSalle & Adams.

  @Test
  public void hasRaspberryOldFashioned() {
    when(dailyDataDAO.save(DailyData.builder()
        .truckId(THEVAULTVANID)
        .onDate(localDate)
        .addSpecial("Raspberry Old Fashioned with Crumble", false)
        .build())).thenReturn(1L);
    when(dailyDataDAO.findByTruckAndDay(THEVAULTVANID, localDate)).thenReturn(null);
    
    specialUpdater.update(THEVAULTVAN, ImmutableList.of(
        Story.builder().text("#VaultVan has raspberry old fashioned with crumble at LaSalle & Adams.").build()));
    
  }
  
  @Test
  public void keylimeCrunch() {
    when(dailyDataDAO.save(DailyData.builder()
        .truckId(THEVAULTVANID)
        .onDate(localDate)
        .addSpecial("Key lime crunch old fashioned", false)
        .build())).thenReturn(1L);
    when(dailyDataDAO.findByTruckAndDay(THEVAULTVANID, localDate)).thenReturn(null);
    
    specialUpdater.update(THEVAULTVAN, ImmutableList.of(
        Story.builder().text("Key lime crunch old fashioned is our special of the day. Come find #VaultVan at Lasalle and Adams this morning.").build()));
    
  }

  @Test
  public void creamCake() {
    when(dailyDataDAO.save(DailyData.builder()
        .truckId(THEVAULTVANID)
        .onDate(localDate)
        .addSpecial("Raspberry Cream Cake", false)
        .build())).thenReturn(1L);
    when(dailyDataDAO.findByTruckAndDay(THEVAULTVANID, localDate)).thenReturn(null);
    
    specialUpdater.update(THEVAULTVAN, ImmutableList.of(
        Story.builder().text("#VaultVan is at LaSalle & Adams with the raspberry cream cake").build()));
    
  }

  @Test
  public void smoresCake() {
    when(dailyDataDAO.save(DailyData.builder()
        .truckId(THEVAULTVANID)
        .onDate(localDate)
        .addSpecial("S'mores Cake", false)
        .build())).thenReturn(1L);
    when(dailyDataDAO.findByTruckAndDay(THEVAULTVANID, localDate)).thenReturn(null);
    
    specialUpdater.update(THEVAULTVAN, ImmutableList.of(
        Story.builder().text("#VaultVan is at Adams and Wacker with our s'mores cake special.").build()));
    
  }

  // #VaultVan has cookies & cream cake and strawberry old fashioned with crumble at LaSalle & Adams.

  @Test
  public void cookiesAndCream() {
    when(dailyDataDAO.save(DailyData.builder()
        .truckId(THEVAULTVANID)
        .onDate(localDate)
        .addSpecial("Cookies n' Cream Cake", false)
        .build())).thenReturn(1L);
    when(dailyDataDAO.findByTruckAndDay(THEVAULTVANID, localDate)).thenReturn(null);
    
    specialUpdater.update(THEVAULTVAN, ImmutableList.of(Story.builder()
        .text("#VaultVan has cookies & cream cake and strawberry old fashioned with crumble at LaSalle & Adams.")
        .build()));
    
  }

  @Test
  public void cookiesAndCream2() {
    when(dailyDataDAO.save(DailyData.builder()
        .truckId(THEVAULTVANID)
        .onDate(localDate)
        .addSpecial("Cookies and Cream Old Fashioned", false)
        .build())).thenReturn(1L);
    when(dailyDataDAO.findByTruckAndDay(THEVAULTVANID, localDate)).thenReturn(null);
    
    specialUpdater.update(THEVAULTVAN, ImmutableList.of(Story.builder()
        .text("#VaultVan is at Adams and Lasalle. Today's special is cookies and cream old fashioned.")
        .build()));
    
  }

  @Test
  public void doubleChocolateYellowCake() {
    when(dailyDataDAO.save(DailyData.builder()
        .truckId(THEVAULTVANID)
        .onDate(localDate)
        .addSpecial("Double Chocolate Yellow Cake", false)
        .build())).thenReturn(1L);
    when(dailyDataDAO.findByTruckAndDay(THEVAULTVANID, localDate)).thenReturn(null);
    
    specialUpdater.update(THEVAULTVAN, ImmutableList.of(
        Story.builder().text("#VaultVan is at Southport & Addison today, serving up hot Metropolis coffee with our double chocolate yellow cake special. Come say hi!\n").build()));
    
  }

  @Test
  public void mochaCoconutCrunch() {
    when(dailyDataDAO.save(DailyData.builder()
        .truckId(THEVAULTVANID)
        .onDate(localDate)
        .addSpecial("Mocha Coconut Crunch Cake", false)
        .build())).thenReturn(1L);
    when(dailyDataDAO.findByTruckAndDay(THEVAULTVANID, localDate)).thenReturn(null);
    
    specialUpdater.update(THEVAULTVAN, ImmutableList.of(
        Story.builder().text("Mocha coconut crunch cake is our special of the day at #VaultVan, posted at LaSalle & Adams for ya.").build()));
    
  }


  @Test
  public void ignoreYourFavorite() {
    when(dailyDataDAO.findByTruckAndDay(THEVAULTVANID, localDate)).thenReturn(null);
    
    specialUpdater.update(THEVAULTVAN, ImmutableList.of(
        Story.builder().text("#VaultVan is at Southport & Addison with your favorite old fashioned flavors. Come and get it!").build()));
    
  }


  @Test
  public void shortCake() {
    when(dailyDataDAO.save(DailyData.builder()
        .truckId(THEVAULTVANID)
        .onDate(localDate)
        .addSpecial("Strawberry Shortcake", false)
        .build())).thenReturn(1L);
    when(dailyDataDAO.findByTruckAndDay(THEVAULTVANID, localDate)).thenReturn(null);
    
    specialUpdater.update(THEVAULTVAN, ImmutableList.of(
        Story.builder().text("Happy Friday, #VaultVan has the strawberry shortcake with crumble.").build()));
    
  }
}