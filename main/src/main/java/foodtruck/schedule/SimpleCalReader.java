package foodtruck.schedule;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.model.TempTruckStop;
import foodtruck.time.Clock;

/**
 * @author aviolette
 * @since 2018-12-11
 */
public class SimpleCalReader {

  private static final Logger log = Logger.getLogger(SimpleCalReader.class.getName());

  private final Clock clock;

  @Inject
  public SimpleCalReader(Clock clock) {
    this.clock = clock;
  }

  public List<TempTruckStop> read(JSONArray arr, String calendarName, String locationName) throws JSONException {
    log.log(Level.INFO, "Scanning calendar: {0}", calendarName);
    ImmutableList.Builder<TempTruckStop> builder = ImmutableList.builder();
    ZonedDateTime now = clock.now8();
    for (int i = 0; i < arr.length(); i++) {
      JSONObject obj = arr.getJSONObject(i);
      ZonedDateTime endTime = ZonedDateTime.from(DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(obj.getString("end")));
      if (endTime.isBefore(now)) {
        continue;
      }
      String title = obj.getString("title");
      String truckId = inferTruckId(title);
      if (truckId == null) {
        continue;
      }
      ZonedDateTime startTime = ZonedDateTime.from(
          DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(obj.getString("start")));
      builder.add(TempTruckStop.builder()
          .truckId(truckId)
          .locationName(locationName)
          .startTime(startTime)
          .endTime(endTime)
          .calendarName(calendarName)
          .build());
    }
    return builder.build();
  }

  @Nullable
  static String inferTruckId(String title) {
    title = title.toLowerCase()
        .replace('’', '\'');
    if (title.contains("bop bar")) {
      return "bopbartruck";
    } else if (title.contains("allegory")) {
      return "allegorynaperville";
    } else if (title.contains("5411 empanadas")) {
      return "5411empanadas";
    } else if (title.contains("arnold's tacos") || title.contains("arnoldstaco")) {
      return "arnoldstacos";
    } else if (title.contains("aztec dave") || title.contains("mexican azteca")) {
      return "aztecdaves";
    } else if (title.contains("jeni's splendid ice cream")) {
      return "jenischi";
    } else if (title.contains("bull grill")) {
      return "bullgrilltruck";
    } else if (title.contains("country grill") || title.contains("rotisserie chicken food truck")) {
      return "countrygrill";
    } else if (title.contains("my funnel truck")) {
      return "myfunneltruck";
    } else if (title.contains("donermen")) {
      return "dmentruck";
    } else if (title.contains("chuck's wood")) {
      return "chuckswoodfired";
    } else if (title.contains("grumpy gaucho") || title.contains("grumpygaucho")) {
      return "grumpygaucho";
    } else if (title.contains("piko street")) {
      return "pikotruck";
    } else if (title.contains("fat shallot")) {
      return "thefatshallot";
    } else if (title.contains("fork-it") || title.contains("dig-in")) {
      return "forkitdigin";
    } else if (title.contains("whadda jerk")) {
      return "whaddajerk";
    } else if (title.contains("puff truck")) {
      return "pufftruckpizza";
    } else if (title.contains("roaming hog") || title.contains("roaminghog")) {
      return "roaminghog";
    } else if (title.contains("blee")) {
      return "bleesmokin";
    } else if (title.contains("mujo ramen")) {
      return "mujoramen";
    } else if (title.contains("corner farmacy")) {
      return "cornerfarmacy";
    } else if (title.contains("umbrella azul")) {
      return "umbrellazul";
    } else if (title.contains("ugly truckin")) {
      return "uglytruckin";
    } else if (title.contains("toastycheese") || title.contains("toasty cheese")) {
      return "mytoastycheese";
    } else if (title.contains("golden eagle hotdogs") || title.contains("golden eagle hot dogs")) {
      return "michaelbatt3";
    } else if (title.contains("best truckin")) {
      return "besttruckinbbq";
    } else if (title.contains("lucy")) {
      return "sohotruck";
    } else if (title.contains("pierogi jo") || title.contains("pierogijos")) {
      return "pierogijos";
    } else if (title.contains("allfiredup") || title.contains("all fired up")) {
      return "chgoallfrup";
    } else if (title.contains("fat tomato")) {
      return "fattomatoinc";
    } else if (title.contains("five squared")) {
      return "fivesquaredfoodtruck";
    } else if (title.contains("little red donut")) {
      return "thelittlereddonuttruck";
    } else if (title.contains("duke’s") || title.contains("duke's")) {
      return "dukesbluesnbbq";
    } else if (title.contains("doctor dogs") || title.contains("dr. dogs") || title.contains("d.d. food truck")) {
      return "doctordogs";
    } else if (title.contains("perk n'") || title.contains("perk n’") || title.contains("perk n pickle")) {
      return "perknpickle";
    } else if (title.contains("smokin' z") || title.contains("smokin z")) {
      return "smokinzbbq";
    } else if (title.contains("olive branch")) {
      return "olivebranchft";
    } else if (title.contains("pizza boss") || title.contains("pizzaboss")) {
      return "chipizzaboss";
    } else if (title.contains("smokin bbq") || title.contains("smokin' bbq")) {
      return "smokinbbqkitchn";
    } else if (title.contains("comodita")) {
      return "ballsoflove";
    } else if (title.contains("mario's cart")) {
      return "marioscart";
    } else if (title.contains("tacos mario")) {
      return "tacosmario";
    } else if (title.contains("twisted classics")) {
      return "twisted_classic";
    } else if (title.contains("tamale spaceship")) {
      return "tamalespaceship";
    } else if (title.contains("happy lobster")) {
      return "thehappylobster";
    } else if (title.contains("yum dum")) {
      return "yumdumtruck";
    } else if (title.contains("your sister's tomato") || title.contains("your sisters tomato")) {
      return "yoursisterstomato";
    } else if (title.contains("babyq")) {
      return "babyqs123";
    } else if (title.contains("fork n' fry") || title.contains("fork n fry")) {
      return "forknfry";
    } else if (title.contains("toasty taco")) {
      return "mytoastytaco";
    } else if (title.contains("three legged") || title.contains("threelegged")) {
      return "threeleggedtaco";
    } else if (title.contains("chicago culinary kitchen")) {
      return "chicagock";
    } else if (title.contains("cocinita")) {
      return "cocinitachicago";
    } else if (title.contains("bricks on wheels") || title.contains("bricks wood fire pizza truck") ||
        title.contains("bricks fire pizza food truck") || title.contains("bricks wood fire pizza food truck") ||
        title.contains("brick's pizza")) {
      return "brickspizza";
    } else if (title.contains("big wang's food truck")) {
      return "bigwangschicago";
    } else if (title.contains("cheesie")) {
      return "cheesies_truck";
    } else if (title.contains("gnarly knots")) {
      return "gnarlyknots";
    } else if (title.contains("rogue food truck")) {
      return "roguefoodtruck";
    } else if (title.contains("ofrenda")) {
      return "ofrendatruck";
    } else if (title.contains("bull & balance") || title.contains("bull and balance")) {
      return "bullandbalance";
    } else if (title.contains("sticks and noodles") || title.contains("stick and noodles") ||
        title.contains("stix and noodles") || title.contains("stix & noodles")) {
      return "stixandnoodles";
    } else if (title.contains("salubrious stop")) {
      return "salubriousstop";
    } else if (title.contains("sausagefest") || title.contains("sausage fest")) {
      return "sausagefestruck";
    } else if (title.contains("jd ") || title.contains("wally") || title.contains("yarn night") ||
        title.contains("matt alfano") || title.contains("sip on") || title.contains("pop-up") ||
        title.contains("dj ") || title.contains("concert") || title.contains("sausage making") ||
        title.contains("open from") || title.contains("music") || title.contains("santa") ||
        title.contains("have a food truck") || title.contains("freddie's off") || title.contains("freddies off") ||
        title.contains("jb's mobile munchies") || title.contains("welcoming") || title.contains("live") ||
        title.contains("closed") || title.contains("tour") || title.contains("yoga") || title.contains("rock out") ||
        title.contains("trivia") || title.contains("beer & yoga") || !title.contains("truck")) {
      return null;
    } else {
      log.log(Level.SEVERE, "Unrecognized truck pattern: {0}", title);
    }

    return null;
  }
}
