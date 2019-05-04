package foodtruck.schedule.custom.chicago;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;

import foodtruck.schedule.SpecialMatcher;
import foodtruck.schedule.Spot;

/**
 * @author aviolette
 * @since 10/5/16
 */
public class ChicagoModule extends AbstractModule {
  @Override
  protected void configure() {
    MapBinder<String, SpecialMatcher> binder = MapBinder.newMapBinder(binder(), String.class, SpecialMatcher.class);
    binder.addBinding("beaversdonuts").to(BeaverMatcher.class);
    binder.addBinding("patronachicago").to(LaJefaMatcher.class);
    binder.addBinding("theroostkitchen").to(RoostMatcher.class);
    binder.addBinding("bobchafoodtruck").to(BobChaMatcher.class);
    binder.addBinding("amanecertacos").to(AmanecerTacosMatcher.class);
    binder.addBinding("thecajuncon").to(CajunConMatcher.class);
    binder.addBinding("thefatshallot").to(FatShallotMatcher.class);
    binder.addBinding("aztecdaves").to(AztecDavesMatcher.class);
    binder.addBinding("thehappylobster").to(HappyLobsterMatcher.class);
  }

  @Provides
  @Singleton
  public ImmutableList<Spot> provideCommonSpots() {
    return ImmutableList.of(new Spot("600w", "600 West Chicago Avenue, Chicago, IL"),
        new Spot("aon", "Randolph and Columbus, Chicago, IL"),
        new Spot("randolph/columbus", "Randolph and Columbus, Chicago, IL"),
        new Spot("wabash/vanburen", "Wabash and Van Buren, Chicago, IL"),
        new Spot("wacker/adams", "Wacker and Adams, Chicago, IL"),
        new Spot("clark/adams", "Clark and Adams, Chicago, IL"),
        new Spot("harrison/michigan", "Michigan and Harrison, Chicago, IL"),
        new Spot("lasalle/adams", "Lasalle and Adams, Chicago, IL"),
        new Spot("clark/monroe", "Clark and Monroe, Chicago, IL"),
        new Spot("wabash/jackson", "Wabash and Jackson, Chicago, IL"),
        new Spot("michigan/monroe", "Michigan and Monroe, Chicago, IL"),
        new Spot("uchicago", "University of Chicago"),
        new Spot("58th/university", "58th and University, Chicago, IL"),
        new Spot("uofc", "University of Chicago"),
        new Spot("adams/58th", "University of Chicago"),
        new Spot("58th/ellis", "University of Chicago"));
  }
}
