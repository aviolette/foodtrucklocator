package foodtruck.alexa;

import java.util.Optional;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.ui.SimpleCard;
import com.amazon.speech.ui.SsmlOutputSpeech;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import foodtruck.dao.LocationDAO;
import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.Location;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;
import foodtruck.schedule.FoodTruckStopService;
import foodtruck.schedule.ScheduleCacher;
import foodtruck.server.resources.json.DailyScheduleWriter;
import foodtruck.time.Clock;

import static com.google.common.truth.Truth.assertThat;
import static foodtruck.alexa.AlexaTestingUtils.assertSpeech;
import static foodtruck.alexa.LocationIntentProcessor.SLOT_LOCATION;
import static foodtruck.alexa.LocationIntentProcessor.SLOT_WHEN;

/**
 * @author aviolette
 * @since 8/25/16
 */
@RunWith(MockitoJUnitRunner.class)
public class LocationIntentProcessorTest extends Mockito {
  private static final String DAILY_SCHEDULE = "{\"trucks\":[{\"id\":\"thevaultvan\",\"iconUrl\":\"http:\\/\\/storage.googleapis.com\\/truckicons\\/doughnutvault.jpeg\",\"twitterHandle\":\"doughnutvault\",\"instagram\":\"\",\"facebook\":\"\\/doughnutvault\",\"foursquare\":\"52443f2d93cd14c4ebc58953\",\"facebookPageId\":\"183806181657622\",\"savory\":false,\"name\":\"Doughnut Vault\",\"yelp\":\"\",\"categories\":[\"Breakfast\",\"Donuts\",\"Coffee\"],\"phone\":\"\",\"description\":\"Serves the old-fashioned and cake donuts from the Doughnut Vault. \",\"url\":\"http:\\/\\/doughnutvault.com\\/\",\"previewIcon\":\"http:\\/\\/storage.googleapis.com\\/truckicons\\/thevaultvan_preview.jpg\",\"inactive\":false,\"menuUrl\":\"\",\"firstSeen\":1379680584000,\"whereFirstSeen\":\"Damen and Wabansia, Chicago, IL\",\"lastSeen\":1473094811000,\"whereLastSeen\":\"Southport and Addison, Chicago, IL\"},{\"id\":\"beaversdonuts\",\"iconUrl\":\"http:\\/\\/storage.googleapis.com\\/truckicons\\/beaversdonuts.JPG\",\"twitterHandle\":\"beaversdonuts\",\"instagram\":\"beaversdonuts\",\"facebook\":\"\\/BeaversDonuts\",\"foursquare\":\"4f2d75d7e4b04796495abac1\",\"facebookPageId\":\"276731655691869\",\"savory\":false,\"name\":\"Beavers Donuts\",\"yelp\":\"beavers-donuts-chicago\",\"categories\":[\"Breakfast\",\"Donuts\",\"Dessert\",\"Coffee\"],\"phone\":\"773-392-1300\",\"description\":\"Three food trucks that serve freshly-fried mini donuts that you can top with the toppings of your choice.\",\"url\":\"http:\\/\\/www.beaversdonuts.com\",\"email\":\"info@beaversdonuts.com\",\"previewIcon\":\"http:\\/\\/storage.googleapis.com\\/truckicons\\/beaversdonuts_preview.jpg\",\"inactive\":false,\"menuUrl\":\"http:\\/\\/beaversdonuts.com\\/Beavers_Donuts_French%20Market%20Menu.pdf\",\"firstSeen\":1324144800000,\"whereFirstSeen\":\"860 North Orleans, Chicago, IL\",\"lastSeen\":1472868000000,\"whereLastSeen\":\"Archer Liquors\"},{\"id\":\"firecakesdonuts\",\"iconUrl\":\"http:\\/\\/storage.googleapis.com\\/truckicons\\/firecakesdonuts.jpeg\",\"twitterHandle\":\"firecakesdonuts\",\"instagram\":\"firecakes\",\"facebook\":\"\\/FirecakesDonuts\",\"foursquare\":\"\",\"facebookPageId\":\"359297440816536\",\"savory\":false,\"name\":\"Firecakes Donuts\",\"yelp\":\"\",\"categories\":[\"Donuts\",\"Breakfast\"],\"phone\":\"312-329-6500\",\"description\":\"Donuts from the Firecakes donut shop on Hubbard.\",\"url\":\"http:\\/\\/firecakesdonuts.com\",\"previewIcon\":\"http:\\/\\/storage.googleapis.com\\/truckicons\\/firecakesdonuts_preview.jpg\",\"inactive\":false,\"menuUrl\":\"\",\"firstSeen\":1422541800000,\"whereFirstSeen\":\"Wacker and Adams, Chicago, IL\",\"lastSeen\":1473094800000,\"whereLastSeen\":\"Southport and Addison, Chicago, IL\"},{\"id\":\"bobchafoodtruck\",\"iconUrl\":\"http:\\/\\/storage.googleapis.com\\/truckicons\\/bobcha_icon.jpg\",\"twitterHandle\":\"bobchafoodtruck\",\"instagram\":\"\",\"facebook\":\"\\/bobchafoodtruck\",\"foursquare\":\"\",\"facebookPageId\":\"1423719477916054\",\"savory\":true,\"name\":\"Bob Cha Food Truck\",\"yelp\":\"\",\"categories\":[\"Asian\",\"Korean\"],\"phone\":\"224-800-3488\",\"description\":\"Two food trucks that serves Korean-fusion tacos and bowls.\",\"url\":\"\",\"email\":\"bobcha.restaurant@gmail.com\",\"previewIcon\":\"http:\\/\\/storage.googleapis.com\\/truckicons\\/bobchafoodtruck_preview.jpg\",\"inactive\":false,\"menuUrl\":\"\",\"firstSeen\":1404576000000,\"whereFirstSeen\":\"2342 North Stockton Drive, Chicago, IL\",\"lastSeen\":1472500804331,\"whereLastSeen\":\"University of Chicago\"},{\"id\":\"brugesbrothers\",\"iconUrl\":\"http:\\/\\/storage.googleapis.com\\/truckicons\\/brugesbrothers.jpeg\",\"twitterHandle\":\"brugesbrothers\",\"instagram\":\"\",\"facebook\":\"\\/brugesbrothers\",\"foursquare\":\"\",\"facebookPageId\":\"308281682688647\",\"savory\":true,\"name\":\"Bruges Brothers\",\"yelp\":\"\",\"categories\":[\"Poutine\",\"Fries\",\"Breakfast\"],\"phone\":\"312-973-3884\",\"description\":\"Food truck serving belgian-style fries with a rotating menu of savory toppings.\",\"url\":\"http:\\/\\/www.brugesbrothers.com\\/menu\\/\",\"previewIcon\":\"http:\\/\\/storage.googleapis.com\\/truckicons\\/brugesbrothers_preview.jpg\",\"inactive\":false,\"menuUrl\":\"https:\\/\\/storage.googleapis.com\\/cftf_menus\\/brugesbrothers.jpg\",\"firstSeen\":1420313400000,\"whereFirstSeen\":\"3627 N Southport, Chicago, IL\",\"lastSeen\":1472846400000,\"whereLastSeen\":\"Daley Plaza\"},{\"id\":\"smokinbbqkitchn\",\"iconUrl\":\"http:\\/\\/storage.googleapis.com\\/truckicons\\/SmokinBBQKitchn.jpg\",\"twitterHandle\":\"smokinbbqkitchn\",\"instagram\":\"\",\"facebook\":\"\",\"foursquare\":\"\",\"facebookPageId\":\"\",\"savory\":true,\"name\":\"Smokin BBQ Kitchen\",\"yelp\":\"\",\"categories\":[\"BBQ\"],\"phone\":\"\",\"description\":\"\",\"url\":\"http:\\/\\/SmokinBBQKitchen.com\",\"previewIcon\":\"http:\\/\\/storage.googleapis.com\\/truckicons\\/smokinbbqkitchn_preview.jpg\",\"inactive\":false,\"menuUrl\":\"\",\"firstSeen\":1472853600000,\"whereFirstSeen\":\"The Open Bottle\",\"lastSeen\":1472868000000,\"whereLastSeen\":\"The Open Bottle\"},{\"id\":\"patronachicago\",\"iconUrl\":\"http:\\/\\/storage.googleapis.com\\/truckicons\\/patronachicago.jpg\",\"twitterHandle\":\"patronachicago\",\"instagram\":\"\",\"facebook\":\"\",\"foursquare\":\"\",\"facebookPageId\":\"\",\"savory\":true,\"name\":\"La Patrona\",\"yelp\":\"\",\"categories\":[\"Mexican\",\"Tacos\",\"Breakfast\"],\"phone\":\"773-229-8005\",\"description\":\"\",\"url\":\"http:\\/\\/www.elpatronchicago.com\",\"email\":\"elpatronxx@icloud.com\",\"previewIcon\":\"http:\\/\\/storage.googleapis.com\\/truckicons\\/patronachicago_preview.jpg\",\"inactive\":false,\"menuUrl\":\"\",\"firstSeen\":1442246400000,\"whereFirstSeen\":\"Wacker and Adams, Chicago, IL\",\"lastSeen\":1472864400000,\"whereLastSeen\":\"Edgewater Beach Food Truck Fridays\"},{\"id\":\"pikotruck\",\"iconUrl\":\"http:\\/\\/storage.googleapis.com\\/truckicons\\/pikotruck.jpeg\",\"twitterHandle\":\"pikotruck\",\"instagram\":\"\",\"facebook\":\"\\/pages\\/PIKO-TRUCK\\/1499851543578015\",\"foursquare\":\"\",\"facebookPageId\":\"1499851543578015\",\"savory\":true,\"name\":\"PIKO Street Kitchen\",\"yelp\":\"\",\"categories\":[\"Asian\"],\"phone\":\"773-809-3312\",\"description\":\"Chicago Area Food Truck serving rice bowls, tacos, sliders, and baos with our Asian Twist!  \",\"url\":\"http:\\/\\/www.pikostreet.com\\/\",\"email\":\"info@pikostreet.com\",\"previewIcon\":\"http:\\/\\/storage.googleapis.com\\/truckicons\\/pikotruck_preview.jpg\",\"inactive\":false,\"menuUrl\":\"https:\\/\\/storage.googleapis.com\\/cftf_menus\\/pikotruckmenu.jpg\",\"firstSeen\":1409346000000,\"whereFirstSeen\":\"Food Truck Extravaganza\",\"lastSeen\":1472580000000,\"whereLastSeen\":\"Wacker and Adams, Chicago, IL\"},{\"id\":\"thefatshallot\",\"iconUrl\":\"http:\\/\\/storage.googleapis.com\\/truckicons\\/thefatshallot.jpeg\",\"twitterHandle\":\"thefatshallot\",\"instagram\":\"thefatshallot\",\"facebook\":\"\\/thefatshallot\",\"foursquare\":\"513fb510e4b0d0bf2081cacc\",\"facebookPageId\":\"290255071103918\",\"savory\":true,\"name\":\"The Fat Shallot\",\"yelp\":\"\",\"categories\":[\"Sandwiches\",\"Fries\",\"Grilled Cheese\"],\"phone\":\"773-893-0826\",\"description\":\"The Fat Shallot serves classic sandwiches such as Grilled Cheese, BLT, Turkey, and Buffalo Chicken along with seasonal additions.\",\"url\":\"http:\\/\\/thefatshallot.com\",\"email\":\"info@thefatshallot.com\",\"previewIcon\":\"http:\\/\\/storage.googleapis.com\\/truckicons\\/thefatshallot_preview.jpg\",\"inactive\":false,\"menuUrl\":\"http:\\/\\/thefatshallot.com\\/menu\\/\",\"firstSeen\":1367941515000,\"whereFirstSeen\":\"57th and Ellis, Chicago, IL\",\"lastSeen\":1473163200000,\"whereLastSeen\":\"University of Chicago\"},{\"id\":\"thefatpickle312\",\"iconUrl\":\"https:\\/\\/storage.googleapis.com\\/truckicons\\/thefatpickle312.jpg\",\"twitterHandle\":\"thefatpickle312\",\"instagram\":\"thefatpickle\",\"facebook\":\"\\/thefatpickle\",\"foursquare\":\"\",\"facebookPageId\":\"\",\"savory\":true,\"name\":\"The Fat Pickle\",\"yelp\":\"\",\"categories\":[\"Sandwiches\"],\"phone\":\"773-893-0826\",\"description\":\"Sister truck of the Fat Shallot that makes amazing deli-style sandwiches.\",\"url\":\"http:\\/\\/thefatpickle.com\\/\",\"previewIcon\":\"https:\\/\\/storage.googleapis.com\\/truckicons\\/thefatpickle312_preview.jpg\",\"inactive\":false,\"menuUrl\":\"https:\\/\\/storage.googleapis.com\\/cftf_menus\\/fatpickle-04142016.jpg\",\"firstSeen\":1459785600000,\"whereFirstSeen\":\"600 West Chicago Avenue, Chicago, IL\",\"lastSeen\":1471633200000,\"whereLastSeen\":\"Randolph and Columbus, Chicago, IL\"},{\"id\":\"boocooroux\",\"iconUrl\":\"https:\\/\\/storage.googleapis.com\\/truckicons\\/BooCooRoux.png\",\"twitterHandle\":\"boocooroux\",\"instagram\":\"\",\"facebook\":\"\\/boocooroux\",\"foursquare\":\"55e0971e498eb5b17717072c\",\"facebookPageId\":\"\",\"savory\":true,\"name\":\"Boocooroux\",\"yelp\":\"\",\"categories\":[\"Cajun\",\"Sandwiches\"],\"phone\":\"773-599-3732\",\"description\":\"Boo Coo Roux serves Cajun and Creole-centric food.\",\"url\":\"http:\\/\\/boocooroux.com\",\"email\":\"info@boocooroux.com\",\"previewIcon\":\"https:\\/\\/storage.googleapis.com\\/truckicons\\/boocooroux_preview.png\",\"inactive\":false,\"menuUrl\":\"\",\"firstSeen\":1440604800000,\"whereFirstSeen\":\"Randolph and Columbus, Chicago, IL\",\"lastSeen\":1472841000000,\"whereLastSeen\":\"Clark and Monroe, Chicago, IL\"},{\"id\":\"chicagolunchbox\",\"iconUrl\":\"http:\\/\\/storage.googleapis.com\\/truckicons\\/chicagolunchbox.png\",\"twitterHandle\":\"chicagolunchbox\",\"instagram\":\"chicagolunchbox\",\"facebook\":\"\\/chicagolunchbox\",\"foursquare\":\"52583c2511d209284fb02c22\",\"facebookPageId\":\"221362701245732\",\"savory\":true,\"name\":\"Chicago Lunch Box\",\"yelp\":\"\",\"categories\":[\"Asian\",\"Vietnamese\"],\"phone\":\"773-696-0144\",\"description\":\"The Chicago Lunchbox serves Vietnamese-style banh mi and rice boxes.\",\"url\":\"http:\\/\\/chicagolunchbox.com\\/\",\"email\":\"chicagolunchbox@gmail.com\",\"previewIcon\":\"http:\\/\\/www.chicagofoodtruckfinder.com\\/images\\/truckicons\\/chicagolunchbox_preview.png\",\"inactive\":false,\"firstSeen\":1379779200000,\"whereFirstSeen\":\"900 N. Branch St, Chicago, IL\",\"lastSeen\":1472842800000,\"whereLastSeen\":\"Daley Plaza\"},{\"id\":\"aztecdaves\",\"iconUrl\":\"http:\\/\\/storage.googleapis.com\\/truckicons\\/aztec_daves.png\",\"twitterHandle\":\"aztec_daves\",\"instagram\":\"aztec_daves\",\"facebook\":\"\\/AztecDaves\",\"foursquare\":\"\",\"facebookPageId\":\"276740742449734\",\"savory\":true,\"name\":\"Aztec Daves\",\"yelp\":\"\",\"categories\":[\"Mexican\",\"Tacos\"],\"phone\":\"\",\"description\":\"Aztec Daves is a taco truck that serves some great tacos with quality ingredients.\",\"url\":\"https:\\/\\/twitter.com\\/Aztec_Daves\",\"email\":\"aztecdaves@gmail.com\",\"previewIcon\":\"http:\\/\\/storage.googleapis.com\\/truckicons\\/aztecdaves_preview.png\",\"inactive\":false,\"menuUrl\":\"https:\\/\\/storage.googleapis.com\\/cftf_menus\\/aztecdavesmenu-04142016.jpg\",\"firstSeen\":1426892400000,\"whereFirstSeen\":\"Quincy and Wood, Chicago, IL\",\"lastSeen\":1472864400000,\"whereLastSeen\":\"Archer Liquors\"},{\"id\":\"dapizzadude1\",\"iconUrl\":\"http:\\/\\/storage.googleapis.com\\/truckicons\\/dapizzadude1.jpeg\",\"twitterHandle\":\"dapizzadude1\",\"instagram\":\"dapizzadude1\",\"facebook\":\"\\/DaPizzaDude\",\"foursquare\":\"\",\"facebookPageId\":\"571305583014134\",\"savory\":true,\"name\":\"Da Pizza Dude\",\"yelp\":\"\",\"categories\":[\"Pizza\",\"Italian\"],\"phone\":\"630-398-6784\",\"description\":\"A dude, his truck, and some delicious, oval-shaped pizzas.\",\"url\":\"http:\\/\\/dapizzadude.com\",\"previewIcon\":\"http:\\/\\/storage.googleapis.com\\/truckicons\\/dapizzadude1_preview.jpg\",\"inactive\":false,\"firstSeen\":1433024870000,\"whereFirstSeen\":\"Ontario and Franklin, Chicago, IL\",\"lastSeen\":1472864400000,\"whereLastSeen\":\"Edgewater Beach Food Truck Fridays\"},{\"id\":\"jsforkintheroad\",\"iconUrl\":\"http:\\/\\/storage.googleapis.com\\/truckicons\\/jsforkintheroad.jpeg\",\"twitterHandle\":\"jsforkintheroad\",\"instagram\":\"jacksforkintheroad\",\"facebook\":\"\\/jacksforkintheroad\",\"foursquare\":\"\",\"facebookPageId\":\"1445302069020627\",\"savory\":true,\"name\":\"Jack's Fork in the Road\",\"yelp\":\"\",\"categories\":[\"Sandwiches\"],\"phone\":\"708-305-5796\",\"description\":\"\",\"url\":\"http:\\/\\/www.jacksforkintheroad.com\\/\",\"previewIcon\":\"http:\\/\\/storage.googleapis.com\\/truckicons\\/jsforkintheroad_preview.jpg\",\"inactive\":false,\"menuUrl\":\"\",\"firstSeen\":1399478400000,\"whereFirstSeen\":\"Madison and Wacker, Chicago, IL\",\"lastSeen\":1472756400000,\"whereLastSeen\":\"Wacker and Adams, Chicago, IL\"},{\"id\":\"thehappylobster\",\"iconUrl\":\"http:\\/\\/storage.googleapis.com\\/truckicons\\/TheHappyLobster.png\",\"twitterHandle\":\"thehappylobster\",\"instagram\":\"thehappylobster\",\"facebook\":\"\\/happylobstertruck\",\"foursquare\":\"\",\"facebookPageId\":\"593322410769802\",\"savory\":true,\"name\":\"Happy Lobster\",\"yelp\":\"\",\"categories\":[\"Lobster\",\"Seafood\"],\"phone\":\"312-485-0342\",\"description\":\"Chicago food truck serving lobster rolls, including an awfully angry one.\",\"url\":\"http:\\/\\/www.happylobstertruck.com\\/\",\"email\":\"info@happylobstertruck.com\",\"previewIcon\":\"http:\\/\\/storage.googleapis.com\\/truckicons\\/thehappylobster_preview.png\",\"inactive\":false,\"menuUrl\":\"\",\"firstSeen\":1433610000000,\"whereFirstSeen\":\"Lagunitas Beer Circus\",\"lastSeen\":1472842800000,\"whereLastSeen\":\"Merchandise Mart\"},{\"id\":\"dmentruck\",\"iconUrl\":\"http:\\/\\/storage.googleapis.com\\/truckicons\\/dmentruck.jpg\",\"twitterHandle\":\"dmentruck\",\"instagram\":\"dmentruck\",\"facebook\":\"\\/dmentruck\",\"foursquare\":\"\",\"facebookPageId\":\"458027034232020\",\"savory\":true,\"name\":\"Döner Men\",\"yelp\":\"\",\"categories\":[\"Sausage\",\"German\",\"Fries\",\"Currywurst\"],\"phone\":\"\",\"description\":\"The Döner Men food truck serves currywurst wraps and boxes.  Poutine is available from time-to-time.\",\"url\":\"http:\\/\\/www.donermen.com\\/\",\"email\":\"catering@donermen.com\",\"previewIcon\":\"http:\\/\\/storage.googleapis.com\\/truckicons\\/dmentruck_preview.jpg\",\"inactive\":false,\"menuUrl\":\"\",\"firstSeen\":1401990097000,\"whereFirstSeen\":\"University of Chicago\",\"lastSeen\":1472841900000,\"whereLastSeen\":\"University of Chicago\"},{\"id\":\"roaminghog\",\"iconUrl\":\"http:\\/\\/storage.googleapis.com\\/truckicons\\/roaminghog.jpg\",\"twitterHandle\":\"roaminghog\",\"instagram\":\"\",\"facebook\":\"\\/The-Roaming-Hog-1699386073627675\\/\",\"foursquare\":\"\",\"facebookPageId\":\"\",\"savory\":true,\"name\":\"The Roaming Hog\",\"yelp\":\"\",\"categories\":[\"BBQ\"],\"phone\":\"847-461-9628\",\"description\":\"\",\"url\":\"\",\"previewIcon\":\"http:\\/\\/storage.googleapis.com\\/truckicons\\/roaminghog_preview.jpg\",\"inactive\":false,\"menuUrl\":\"\",\"firstSeen\":1460239200000,\"whereFirstSeen\":\"Noon Whistle Brewing\",\"lastSeen\":1472950800000,\"whereLastSeen\":\"Vice District Brewing\"}],\"locations\":[{\"latitude\":41.879267999999996,\"longitude\":-87.63234899999999,\"url\":\"\",\"radius\":0,\"radiateTo\":3,\"description\":\"The food truck stand is on the southeast corner of Lasalle and Adams\",\"name\":\"Lasalle and Adams, Chicago, IL\",\"hasBooze\":false,\"closed\":false,\"popular\":true,\"email\":\"\",\"facebookUri\":\"\",\"phone\":\"\",\"designatedStop\":true,\"imageUrl\":\"https:\\/\\/storage.googleapis.com\\/cftf_locationicons\\/lasalleadams.jpg\",\"twitterHandle\":\"chiftf_cbot\",\"alexaProvided\":true,\"key\":2174002,\"id\":2},{\"latitude\":41.878057,\"longitude\":-87.626099,\"url\":\"\",\"radius\":0,\"radiateTo\":3,\"description\":\"\",\"name\":\"Wabash and Jackson, Chicago, IL\",\"hasBooze\":false,\"closed\":false,\"popular\":false,\"email\":\"\",\"facebookUri\":\"\",\"phone\":\"\",\"designatedStop\":true,\"twitterHandle\":\"chiftf_wabvan\",\"alexaProvided\":false,\"key\":501097,\"id\":3},{\"latitude\":41.881879999999995,\"longitude\":-87.63650799999999,\"url\":\"\",\"radius\":0.051,\"radiateTo\":4,\"description\":\"\",\"name\":\"Madison and Wacker, Chicago, IL\",\"hasBooze\":false,\"closed\":false,\"popular\":true,\"email\":\"\",\"facebookUri\":\"\",\"phone\":\"\",\"designatedStop\":false,\"twitterHandle\":\"chiftf_madwack\",\"alexaProvided\":true,\"key\":555287,\"id\":4},{\"latitude\":41.880506,\"longitude\":-87.630867,\"url\":\"\",\"radius\":0.08,\"radiateTo\":3,\"description\":\"\",\"name\":\"Clark and Monroe, Chicago, IL\",\"hasBooze\":false,\"closed\":false,\"popular\":true,\"email\":\"\",\"facebookUri\":\"\",\"phone\":\"\",\"designatedStop\":true,\"imageUrl\":\"https:\\/\\/storage.googleapis.com\\/cftf_locationicons\\/chase.jpg\",\"twitterHandle\":\"chiftf_125clark\",\"alexaProvided\":true,\"key\":51101,\"id\":5},{\"latitude\":41.790628999999996,\"longitude\":-87.60130099999999,\"url\":\"\",\"radius\":0.2,\"radiateTo\":3,\"description\":\"The food trucks line up on Ellis from roughly 57th through 59th street.\",\"name\":\"University of Chicago\",\"hasBooze\":false,\"closed\":false,\"popular\":true,\"email\":\"\",\"facebookUri\":\"\",\"phone\":\"\",\"designatedStop\":false,\"imageUrl\":\"https:\\/\\/storage.googleapis.com\\/cftf_locationicons\\/uofc.jpg\",\"twitterHandle\":\"chiftf_uchicago\",\"alexaProvided\":true,\"key\":6259051008622592,\"id\":6},{\"latitude\":41.889685,\"longitude\":-87.622058,\"url\":\"\",\"radius\":0.11,\"radiateTo\":1,\"description\":\"\",\"name\":\"450 N. Cityfront Plaza, Chicago, IL\",\"hasBooze\":false,\"closed\":false,\"popular\":true,\"email\":\"\",\"facebookUri\":\"\",\"phone\":\"\",\"designatedStop\":true,\"imageUrl\":\"https:\\/\\/storage.googleapis.com\\/cftf_locationicons\\/nbc.jpg\",\"twitterHandle\":\"chiftf_nbctower\",\"alexaProvided\":true,\"key\":2231491,\"id\":7},{\"latitude\":41.590241999999996,\"longitude\":-87.693056,\"radius\":0,\"radiateTo\":0,\"name\":\"16501 Kedzie Avenue, Markham, IL\",\"hasBooze\":false,\"closed\":false,\"popular\":false,\"designatedStop\":false,\"alexaProvided\":false,\"key\":5810612923793408,\"id\":8},{\"latitude\":41.879385,\"longitude\":-87.63697499999999,\"url\":\"\",\"radius\":0,\"radiateTo\":1,\"description\":\"\",\"name\":\"Wacker and Adams, Chicago, IL\",\"hasBooze\":false,\"closed\":false,\"popular\":false,\"email\":\"\",\"facebookUri\":\"\",\"phone\":\"\",\"designatedStop\":false,\"twitterHandle\":\"chiftf_willis\",\"alexaProvided\":true,\"key\":28065,\"id\":9},{\"latitude\":41.880857,\"longitude\":-87.623599,\"url\":\"\",\"radius\":0.15,\"radiateTo\":4,\"description\":\"\",\"name\":\"Michigan and Monroe, Chicago, IL\",\"hasBooze\":false,\"closed\":false,\"popular\":true,\"email\":\"\",\"facebookUri\":\"\",\"phone\":\"\",\"designatedStop\":true,\"imageUrl\":\"https:\\/\\/storage.googleapis.com\\/cftf_locationicons\\/michmon.jpg\",\"twitterHandle\":\"chiftf_michmon\",\"alexaProvided\":true,\"key\":5732531110412288,\"id\":10},{\"latitude\":41.884851999999995,\"longitude\":-87.620764,\"url\":\"\",\"radius\":0.12,\"radiateTo\":0,\"description\":\"Food trucks usually park north of that intersection on Randolph.\",\"name\":\"Randolph and Columbus, Chicago, IL\",\"hasBooze\":false,\"closed\":false,\"popular\":true,\"email\":\"\",\"facebookUri\":\"\",\"phone\":\"\",\"designatedStop\":false,\"imageUrl\":\"https:\\/\\/storage.googleapis.com\\/cftf_locationicons\\/aon.jpg\",\"twitterHandle\":\"chiftf_aon\",\"alexaProvided\":true,\"key\":50018,\"id\":11},{\"latitude\":41.774843,\"longitude\":-88.188863,\"url\":\"http:\\/\\/solemnoathbrewery.com\\/taproom\\/\",\"radius\":0,\"radiateTo\":0,\"description\":\"1661 Quincy Ave #179, Naperville, IL\\n\",\"name\":\"Solemn Oath Brewery\",\"hasBooze\":true,\"closed\":false,\"popular\":false,\"email\":\"\",\"facebookUri\":\"\\/solemnoathbeer\",\"phone\":\"\",\"designatedStop\":false,\"imageUrl\":\"https:\\/\\/storage.googleapis.com\\/cftf_locationicons\\/solemnoathbeer.jpeg\",\"twitterHandle\":\"solemnoathbeer\",\"alexaProvided\":true,\"key\":1835344,\"id\":12}],\"stops\":[{\"key\":\"6500088050352128\",\"location\":1,\"truckId\":\"thevaultvan\",\"confidence\":\"MEDIUM\",\"startTime\":\"06:30 AM\",\"startMillis\":1473161400000,\"endMillis\":1473188400000,\"endTime\":\"02:00 PM\"},{\"key\":\"5082341089214464\",\"location\":2,\"truckId\":\"beaversdonuts\",\"confidence\":\"MEDIUM\",\"startTime\":\"07:00 AM\",\"startMillis\":1473163200000,\"endMillis\":1473202800000,\"endTime\":\"06:00 PM\"},{\"key\":\"5705899205197824\",\"location\":3,\"truckId\":\"beaversdonuts\",\"confidence\":\"MEDIUM\",\"startTime\":\"07:00 AM\",\"startMillis\":1473163200000,\"endMillis\":1473174000000,\"endTime\":\"10:00 AM\"},{\"key\":\"5133025461403648\",\"location\":4,\"truckId\":\"firecakesdonuts\",\"confidence\":\"MEDIUM\",\"startTime\":\"07:58 AM\",\"startMillis\":1473166722000,\"endMillis\":1473188400000,\"endTime\":\"02:00 PM\"},{\"key\":\"5203745856028672\",\"location\":5,\"truckId\":\"bobchafoodtruck\",\"confidence\":\"MEDIUM\",\"startTime\":\"10:34 AM\",\"startMillis\":1473176056000,\"endMillis\":1473186600000,\"endTime\":\"01:30 PM\"},{\"key\":\"5632557269909504\",\"location\":6,\"truckId\":\"brugesbrothers\",\"confidence\":\"MEDIUM\",\"startTime\":\"10:50 AM\",\"startMillis\":1473177050000,\"endMillis\":1473188400000,\"endTime\":\"02:00 PM\"},{\"key\":\"4811238190088192\",\"location\":7,\"truckId\":\"smokinbbqkitchn\",\"confidence\":\"MEDIUM\",\"startTime\":\"11:00 AM\",\"startMillis\":1473177600000,\"endMillis\":1473188400000,\"endTime\":\"02:00 PM\"},{\"key\":\"5136587465687040\",\"location\":4,\"truckId\":\"patronachicago\",\"confidence\":\"MEDIUM\",\"startTime\":\"11:00 AM\",\"startMillis\":1473177600000,\"endMillis\":1473188400000,\"endTime\":\"02:00 PM\"},{\"key\":\"5651236619550720\",\"location\":8,\"truckId\":\"patronachicago\",\"confidence\":\"MEDIUM\",\"startTime\":\"11:00 AM\",\"startMillis\":1473177600000,\"endMillis\":1473188400000,\"endTime\":\"02:00 PM\"},{\"key\":\"5668547753672704\",\"location\":8,\"truckId\":\"pikotruck\",\"confidence\":\"MEDIUM\",\"startTime\":\"11:00 AM\",\"startMillis\":1473177600000,\"endMillis\":1473184800000,\"endTime\":\"01:00 PM\"},{\"key\":\"5685212226781184\",\"location\":5,\"truckId\":\"patronachicago\",\"confidence\":\"MEDIUM\",\"startTime\":\"11:00 AM\",\"startMillis\":1473177600000,\"endMillis\":1473188400000,\"endTime\":\"02:00 PM\"},{\"key\":\"5689564639264768\",\"location\":9,\"truckId\":\"beaversdonuts\",\"confidence\":\"MEDIUM\",\"startTime\":\"11:00 AM\",\"startMillis\":1473177600000,\"endMillis\":1473202800000,\"endTime\":\"06:00 PM\"},{\"key\":\"5876836756094976\",\"location\":5,\"truckId\":\"thefatshallot\",\"confidence\":\"MEDIUM\",\"startTime\":\"11:00 AM\",\"startMillis\":1473177600000,\"endMillis\":1473188400000,\"endTime\":\"02:00 PM\"},{\"key\":\"6350488433852416\",\"location\":10,\"truckId\":\"thefatpickle312\",\"confidence\":\"MEDIUM\",\"startTime\":\"11:00 AM\",\"startMillis\":1473177600000,\"endMillis\":1473188400000,\"endTime\":\"02:00 PM\"},{\"key\":\"5664352107495424\",\"location\":10,\"truckId\":\"boocooroux\",\"confidence\":\"MEDIUM\",\"startTime\":\"11:05 AM\",\"startMillis\":1473177904000,\"endMillis\":1473185700000,\"endTime\":\"01:15 PM\"},{\"key\":\"5648321410498560\",\"location\":5,\"truckId\":\"chicagolunchbox\",\"confidence\":\"MEDIUM\",\"startTime\":\"11:19 AM\",\"startMillis\":1473178771000,\"endMillis\":1473184800000,\"endTime\":\"01:00 PM\"},{\"key\":\"5668490576920576\",\"location\":8,\"truckId\":\"aztecdaves\",\"confidence\":\"MEDIUM\",\"startTime\":\"11:30 AM\",\"startMillis\":1473179400000,\"endMillis\":1473186600000,\"endTime\":\"01:30 PM\"},{\"key\":\"5696407864344576\",\"location\":8,\"truckId\":\"dapizzadude1\",\"confidence\":\"MEDIUM\",\"startTime\":\"11:30 AM\",\"startMillis\":1473179400000,\"endMillis\":1473186600000,\"endTime\":\"01:30 PM\"},{\"key\":\"5726887770849280\",\"location\":8,\"truckId\":\"jsforkintheroad\",\"confidence\":\"MEDIUM\",\"startTime\":\"11:30 AM\",\"startMillis\":1473179400000,\"endMillis\":1473186600000,\"endTime\":\"01:30 PM\"},{\"key\":\"5762324069613568\",\"location\":4,\"truckId\":\"thehappylobster\",\"confidence\":\"MEDIUM\",\"startTime\":\"11:30 AM\",\"startMillis\":1473179400000,\"endMillis\":1473186600000,\"endTime\":\"01:30 PM\"},{\"key\":\"5665066414243840\",\"location\":5,\"truckId\":\"dmentruck\",\"confidence\":\"MEDIUM\",\"startTime\":\"11:50 AM\",\"startMillis\":1473180658000,\"endMillis\":1473187858000,\"endTime\":\"01:50 PM\"},{\"key\":\"6031867090305024\",\"location\":11,\"truckId\":\"roaminghog\",\"confidence\":\"MEDIUM\",\"startTime\":\"05:00 PM\",\"startMillis\":1473199200000,\"endMillis\":1473210000000,\"endTime\":\"08:00 PM\"}],\"date\":\"20160906\",\"specials\":[{\"truckId\":\"thevaultvan\",\"date\":\"20160906\",\"specials\":[{\"special\":\"Blueberry Old-Fashioned With Crumble\",\"soldout\":false}]}]}, \"KbCD7Qb9\", \"Chicago, IL\");\n" + "  });";
  private static final String CLARK_N_MONROE = "Clark and Monroe";
  private static final String TOMORROW = "tomorrow";
  private static final String TODAY = "today";
  private static final Truck BEAVERS = Truck.builder()
      .name("Beavers Donuts")
      .build();
  private static final TruckStop BEAVERS_STOP = TruckStop.builder()
      .truck(BEAVERS)
      .startTime(new DateTime(2016, 9, 1, 7, 0))
      .endTime(new DateTime(2016, 9, 1, 10, 0))
      .build();
  private static final Truck THE_FAT_PICKLE = Truck.builder()
      .name("The Fat Pickle")
      .build();
  private static final TruckStop FAT_PICKLE_STOP = TruckStop.builder()
      .truck(THE_FAT_PICKLE)
      .startTime(new DateTime(2016, 9, 1, 11, 0))
      .endTime(new DateTime(2016, 9, 1, 13, 0))
      .build();
  private static final Truck CHICAGOS_FINEST = Truck.builder()
      .name("Chicagos Finest")
      .build();
  private static final TruckStop CHICAGOS_FINEST_STOP = TruckStop.builder()
      .truck(CHICAGOS_FINEST)
      .startTime(new DateTime(2016, 9, 1, 11, 0))
      .endTime(new DateTime(2016, 9, 1, 13, 0))
      .build();
  @Mock private GeoLocator locator;
  @Mock private FoodTruckStopService service;
  private LocationIntentProcessor processor;
  private DateTime date;
  private Location location;
  @Mock private LocationDAO locationDAO;
  @Mock private ScheduleCacher cacher;

  @Before
  public void before() {
    Clock clock = mock(Clock.class);
    DailyScheduleWriter dailyScheduleWriter = mock(DailyScheduleWriter.class);
    date = new DateTime(2016, 7, 15, 10, 10);
    location = Location.builder()
        .name(CLARK_N_MONROE)
        .lat(12)
        .lng(13)
        .build();
    Location defaultCenter = Location.builder()
        .name("Wacker and Adams")
        .lat(-12)
        .lng(-13)
        .build();
    when(clock.now()).thenReturn(date);
    when(clock.currentDay()).thenReturn(date.toLocalDate());
    processor = new LocationIntentProcessor(locator, service, clock, locationDAO, cacher, defaultCenter,
        DateTimeFormat.forPattern("hh:mm a"), false);
  }

  /**
   * Search location resolves.  There are no trucks at that location. There are no alternates within
   * range.
   */
  @Test
  public void processWithNoDateNoTrucksNoAlternates() throws Exception {
    when(locator.locate(CLARK_N_MONROE, GeolocationGranularity.NARROW)).thenReturn(location);
    when(service.findStopsNearALocation(location, date.toLocalDate())).thenReturn(ImmutableList.of());
    when(cacher.findSchedule()).thenReturn(DAILY_SCHEDULE);
    dailyScheduleNullLocations();
    when(locationDAO.findByIdOpt(28065L)).thenReturn(Optional.empty());
    when(locationDAO.findByIdOpt(5732531110412288L)).thenReturn(Optional.empty());
    when(locationDAO.findByIdOpt(50018L)).thenReturn(Optional.empty());
    when(locationDAO.findByIdOpt(1835344L)).thenReturn(Optional.empty());
    
    SpeechletResponse response = processor.process(intent(CLARK_N_MONROE, null), null);
    assertThat(response.getCard()
        .getTitle()).isEqualTo("Food Trucks at Clark and Monroe Today");
    assertSpeech(response.getOutputSpeech()).isEqualTo(
        "<speak>There are no trucks at Clark and Monroe today and there don't appear to be any nearby that location.</speak>");
    
  }

  /**
   * Special case that happens a lot: Alexa sometimes includes 'for' at end of name.
   */
  @Test
  public void processStripFor() throws Exception {
    when(locator.locate(CLARK_N_MONROE, GeolocationGranularity.NARROW)).thenReturn(location);
    when(service.findStopsNearALocation(location, date.toLocalDate())).thenReturn(ImmutableList.of());
    when(cacher.findSchedule()).thenReturn(DAILY_SCHEDULE);
    dailyScheduleNullLocations();
    when(locationDAO.findByIdOpt(28065L)).thenReturn(Optional.empty());
    when(locationDAO.findByIdOpt(5732531110412288L)).thenReturn(Optional.empty());
    when(locationDAO.findByIdOpt(50018L)).thenReturn(Optional.empty());
    when(locationDAO.findByIdOpt(1835344L)).thenReturn(Optional.empty());
    
    SpeechletResponse response = processor.process(intent("Clark and Monroe for", null), null);
    assertThat(response.getCard()
        .getTitle()).isEqualTo("Food Trucks at Clark and Monroe Today");
    assertSpeech(response.getOutputSpeech()).isEqualTo(
        "<speak>There are no trucks at Clark and Monroe today and there don't appear to be any nearby that location.</speak>");
    
  }

  /**
   * Search location does not resolve.  There are no alternatives.
   */
  @Test
  public void processWithNoDateNoTruckUnresolved() throws Exception {
    when(locator.locate(CLARK_N_MONROE, GeolocationGranularity.NARROW)).thenReturn(null);
    when(cacher.findSchedule()).thenReturn(DAILY_SCHEDULE);
    dailyScheduleNullLocations();
    when(locationDAO.findById(28065L)).thenReturn(null);
    when(locationDAO.findById(5732531110412288L)).thenReturn(null);
    when(locationDAO.findById(50018L)).thenReturn(null);
    when(locationDAO.findById(1835344L)).thenReturn(null);
    
    SpeechletResponse response = processor.process(intent(CLARK_N_MONROE, null), null);
    assertSpeech(response.getOutputSpeech()).isEqualTo("<speak>What location was that?</speak>");
    
  }

  private void dailyScheduleNullLocations() {
    when(locationDAO.findByIdOpt(2174002L)).thenReturn(Optional.empty());
    when(locationDAO.findByIdOpt(501097L)).thenReturn(Optional.empty());
    when(locationDAO.findByIdOpt(555287L)).thenReturn(Optional.empty());
    when(locationDAO.findByIdOpt(51101L)).thenReturn(Optional.empty());
    when(locationDAO.findByIdOpt(6259051008622592L)).thenReturn(Optional.empty());
    when(locationDAO.findByIdOpt(2231491L)).thenReturn(Optional.empty());
    when(locationDAO.findByIdOpt(5810612923793408L)).thenReturn(Optional.empty());
  }

  @Test
  public void procesWithNoDateOneTruck() throws Exception {
    when(locator.locate(CLARK_N_MONROE, GeolocationGranularity.NARROW)).thenReturn(location);
    when(service.findStopsNearALocation(location, date.toLocalDate())).thenReturn(ImmutableList.of(FAT_PICKLE_STOP));
    
    SpeechletResponse response = processor.process(intent(CLARK_N_MONROE, null), null);
    assertThat(response.getCard()
        .getTitle()).isEqualTo("Food Trucks at Clark and Monroe Today");
    assertThat(((SsmlOutputSpeech) response.getOutputSpeech()).getSsml()).isEqualTo(
        "<speak>The Fat Pickle is at Clark and Monroe today from 11:00 AM to 01:00 PM</speak>");
    
  }

  @Test
  public void processWithNoDateTwoTrucks() throws Exception {
    when(locator.locate(CLARK_N_MONROE, GeolocationGranularity.NARROW)).thenReturn(location);
    when(service.findStopsNearALocation(location, date.toLocalDate())).thenReturn(
        ImmutableList.of(BEAVERS_STOP, FAT_PICKLE_STOP));
    
    SpeechletResponse response = processor.process(intent(CLARK_N_MONROE, null), null);
    assertThat(response.getCard()
        .getTitle()).isEqualTo("Food Trucks at Clark and Monroe Today");
    assertThat(((SsmlOutputSpeech) response.getOutputSpeech()).getSsml()).isEqualTo(
        "<speak>Beavers Donuts and The Fat Pickle are at Clark and Monroe today. Beavers Donuts from 07:00 AM to 10:00 AM and The Fat Pickle from 11:00 AM to 01:00 PM</speak>");
    
  }


  @Test
  public void processWithNoDateTwoTrucksSameTime() throws Exception {
    when(locator.locate(CLARK_N_MONROE, GeolocationGranularity.NARROW)).thenReturn(location);
    when(service.findStopsNearALocation(location, date.toLocalDate())).thenReturn(
        ImmutableList.of(CHICAGOS_FINEST_STOP, FAT_PICKLE_STOP));
    
    SpeechletResponse response = processor.process(intent(CLARK_N_MONROE, null), null);
    assertThat(response.getCard()
        .getTitle()).isEqualTo("Food Trucks at Clark and Monroe Today");
    assertThat(((SsmlOutputSpeech) response.getOutputSpeech()).getSsml()).isEqualTo(
        "<speak>Chicagos Finest and The Fat Pickle are at Clark and Monroe today from 11:00 AM to 01:00 PM</speak>");
    
  }


  @Test
  public void processWithNoDateThreeTrucks() throws Exception {
    ;
    when(locator.locate(CLARK_N_MONROE, GeolocationGranularity.NARROW)).thenReturn(location);
    when(service.findStopsNearALocation(location, date.toLocalDate())).thenReturn(
        ImmutableList.of(BEAVERS_STOP, CHICAGOS_FINEST_STOP, FAT_PICKLE_STOP));
    
    SpeechletResponse response = processor.process(intent(CLARK_N_MONROE, null), null);
    assertThat(response.getCard()
        .getTitle()).isEqualTo("Food Trucks at Clark and Monroe Today");
    assertThat(((SsmlOutputSpeech) response.getOutputSpeech()).getSsml()).isEqualTo(
        "<speak>There are 3 trucks at Clark and Monroe today: Beavers Donuts,<break time=\"0.3s\"/> Chicagos Finest, and The Fat Pickle. Beavers Donuts from 07:00 AM to 10:00 AM,<break time=\"0.3s\"/> Chicagos Finest from 11:00 AM to 01:00 PM, and The Fat Pickle from 11:00 AM to 01:00 PM</speak>");
    
  }

  @Test
  public void processOneToday() throws Exception {
    when(locator.locate(CLARK_N_MONROE, GeolocationGranularity.NARROW)).thenReturn(location);
    when(service.findStopsNearALocation(location, date.toLocalDate())).thenReturn(ImmutableList.of(FAT_PICKLE_STOP));
    
    SpeechletResponse response = processor.process(intent(CLARK_N_MONROE, TODAY), null);
    assertThat(response.getCard()
        .getTitle()).isEqualTo("Food Trucks at Clark and Monroe Today");
    assertSpeech(response.getOutputSpeech()).isEqualTo(
        "<speak>The Fat Pickle is at Clark and Monroe today from 11:00 AM to 01:00 PM</speak>");
    
  }

  @Test
  public void processTwoToday() throws Exception {
    when(locator.locate(CLARK_N_MONROE, GeolocationGranularity.NARROW)).thenReturn(location);
    when(service.findStopsNearALocation(location, date.toLocalDate())).thenReturn(
        ImmutableList.of(BEAVERS_STOP, FAT_PICKLE_STOP));
    
    SpeechletResponse response = processor.process(intent(CLARK_N_MONROE, TODAY), null);
    assertThat(response.getCard()
        .getTitle()).isEqualTo("Food Trucks at Clark and Monroe Today");
    assertSpeech(response.getOutputSpeech()).isEqualTo(
        "<speak>Beavers Donuts and The Fat Pickle are at Clark and Monroe today. Beavers Donuts from 07:00 AM to 10:00 AM and The Fat Pickle from 11:00 AM to 01:00 PM</speak>");
    
  }

  @Test
  public void processThreeToday() throws Exception {
    when(locator.locate(CLARK_N_MONROE, GeolocationGranularity.NARROW)).thenReturn(location);
    when(service.findStopsNearALocation(location, date.toLocalDate())).thenReturn(
        ImmutableList.of(BEAVERS_STOP, CHICAGOS_FINEST_STOP, FAT_PICKLE_STOP));
    
    SpeechletResponse response = processor.process(intent(CLARK_N_MONROE, TODAY), null);
    assertThat(response.getCard()
        .getTitle()).isEqualTo("Food Trucks at Clark and Monroe Today");
    assertSpeech(response.getOutputSpeech()).isEqualTo(
        "<speak>There are 3 trucks at Clark and Monroe today: Beavers Donuts,<break time=\"0.3s\"/> Chicagos Finest, and The Fat Pickle. Beavers Donuts from 07:00 AM to 10:00 AM,<break time=\"0.3s\"/> Chicagos Finest from 11:00 AM to 01:00 PM, and The Fat Pickle from 11:00 AM to 01:00 PM</speak>");
    
  }

  @Test
  public void processOneTomorrow() throws Exception {
    Intent intent = intent(CLARK_N_MONROE, TOMORROW);
    LocalDate localDate = date.toLocalDate()
        .withDayOfMonth(16);
    when(locator.locate(CLARK_N_MONROE, GeolocationGranularity.NARROW)).thenReturn(location);
    when(service.findStopsNearALocation(location, localDate)).thenReturn(ImmutableList.of(FAT_PICKLE_STOP));
    
    SpeechletResponse response = processor.process(intent, null);
    assertThat(response.getCard()
        .getTitle()).isEqualTo("Food Trucks at Clark and Monroe Tomorrow");
    assertSpeech(response.getOutputSpeech()).isEqualTo(
        "<speak>The Fat Pickle is scheduled to be at Clark and Monroe tomorrow from 11:00 AM to 01:00 PM</speak>");
    
  }

  @Test
  public void processTwoTomorrow() throws Exception {
    Intent intent = intent(CLARK_N_MONROE, TOMORROW);
    date = date.withDayOfMonth(16);
    when(locator.locate(CLARK_N_MONROE, GeolocationGranularity.NARROW)).thenReturn(location);
    when(service.findStopsNearALocation(location, date.toLocalDate())).thenReturn(
        ImmutableList.of(BEAVERS_STOP, FAT_PICKLE_STOP));
    
    SpeechletResponse response = processor.process(intent, null);
    assertThat(response.getCard()
        .getTitle()).isEqualTo("Food Trucks at Clark and Monroe Tomorrow");
    assertSpeech(response.getOutputSpeech()).isEqualTo(
        "<speak>Beavers Donuts and The Fat Pickle are scheduled to be at Clark and Monroe tomorrow. Beavers Donuts from 07:00 AM to 10:00 AM and The Fat Pickle from 11:00 AM to 01:00 PM</speak>");
    
  }

  @Test
  public void processThreeTomorrow() throws Exception {
    Intent intent = intent(CLARK_N_MONROE, TOMORROW);
    date = date.withDayOfMonth(16);
    when(locator.locate(CLARK_N_MONROE, GeolocationGranularity.NARROW)).thenReturn(location);
    when(service.findStopsNearALocation(location, date.toLocalDate())).thenReturn(
        ImmutableList.of(BEAVERS_STOP, CHICAGOS_FINEST_STOP, FAT_PICKLE_STOP));
    
    SpeechletResponse response = processor.process(intent, null);
    assertThat(response.getCard()
        .getTitle()).isEqualTo("Food Trucks at Clark and Monroe Tomorrow");
    assertSpeech(response.getOutputSpeech()).isEqualTo(
        "<speak>There are 3 trucks scheduled to be at Clark and Monroe tomorrow: Beavers Donuts,<break time=\"0.3s\"/> Chicagos Finest, and The Fat Pickle. Beavers Donuts from 07:00 AM to 10:00 AM,<break time=\"0.3s\"/> Chicagos Finest from 11:00 AM to 01:00 PM, and The Fat Pickle from 11:00 AM to 01:00 PM</speak>");
    assertThat(((SimpleCard) response.getCard()).getContent()).isEqualTo(
        "There are 3 trucks scheduled to be at Clark and Monroe tomorrow: Beavers Donuts, Chicagos Finest, and The Fat Pickle. Beavers Donuts from 07:00 AM to 10:00 AM, Chicagos Finest from 11:00 AM to 01:00 PM, and The Fat Pickle from 11:00 AM to 01:00 PM");

  }

  private Intent intent(String name, String when) {
    return Intent.builder()
        .withName(AlexaModule.GET_FOOD_TRUCKS_AT_LOCATION)
        .withSlots(ImmutableMap.of(SLOT_LOCATION, Slot.builder()
            .withName(SLOT_LOCATION)
            .withValue(name)
            .build(), SLOT_WHEN, Slot.builder()
            .withName(SLOT_WHEN)
            .withValue(when)
            .build()))
        .build();
  }

}