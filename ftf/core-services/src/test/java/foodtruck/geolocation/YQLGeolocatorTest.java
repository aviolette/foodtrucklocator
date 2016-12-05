package foodtruck.geolocation;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import foodtruck.model.Location;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author aviolette
 * @since 4/29/13
 */
@RunWith(MockitoJUnitRunner.class)
public class YQLGeolocatorTest {
  private @Mock YQLResource resource;
  private YQLGeolocator yqlGeolocator;

  @Before
  public void before() {
    yqlGeolocator = new YQLGeolocator(resource);
  }

  @Test
  public void findLocation() throws JSONException {
    String location = "1504 North Western Avenue, Chicago";
    JSONObject jsonObject = new JSONObject("{\"query\": {\n" +
        "  \"count\": 1,\n" +
        "  \"created\": \"2013-04-29T11:43:06Z\",\n" +
        "  \"lang\": \"en-US\",\n" +
        "  \"results\": {\"Result\": {\n" +
        "    \"quality\": \"87\",\n" +
        "    \"latitude\": \"41.908741\",\n" +
        "    \"longitude\": \"-87.687149\",\n" +
        "    \"offsetlat\": \"41.90873\",\n" +
        "    \"offsetlon\": \"-87.687561\",\n" +
        "    \"radius\": \"400\",\n" +
        "    \"name\": null,\n" +
        "    \"line1\": \"1504 N Western Ave\",\n" +
        "    \"line2\": \"Chicago, IL 60622-1746\",\n" +
        "    \"line3\": null,\n" +
        "    \"line4\": \"United States\",\n" +
        "    \"house\": \"1504\",\n" +
        "    \"street\": \"N Western Ave\",\n" +
        "    \"xstreet\": null,\n" +
        "    \"unittype\": null,\n" +
        "    \"unit\": null,\n" +
        "    \"postal\": \"60622-1746\",\n" +
        "    \"neighborhood\": null,\n" +
        "    \"city\": \"Chicago\",\n" +
        "    \"county\": \"Cook County\",\n" +
        "    \"state\": \"Illinois\",\n" +
        "    \"country\": \"United States\",\n" +
        "    \"countrycode\": \"US\",\n" +
        "    \"statecode\": \"IL\",\n" +
        "    \"countycode\": null,\n" +
        "    \"uzip\": \"60622\",\n" +
        "    \"hash\": \"3CA8E5C34858988D\",\n" +
        "    \"woeid\": \"12784276\",\n" +
        "    \"woetype\": \"11\"\n" +
        "  }}\n" +
        "}}\n");
    when(resource.findLocation(location, false)).thenReturn(jsonObject);
    Location loc = yqlGeolocator.locate(location, GeolocationGranularity.NARROW);
    assertThat(loc).isNotNull();
    assertThat(loc.getName()).isEqualTo(location);
    assertThat(loc.getLatitude()).isWithin(0.00001).of(41.908741);
    assertThat(loc.getLongitude()).isWithin(0.00001).of(-87.687149);
  }

  @Test
  public void broadSearchWithBroadResults() throws Exception {
    String location = "1504 North Western Avenue, Chicago";
    JSONObject jsonObject = new JSONObject("{\"query\":{\"count\":1,\"created\":\"2014-01-31T22:30:02Z\",\"lang\":\"en-US\",\"results\":{\"Result\":{\"quality\":\"62\",\"latitude\":\"41.787498\",\"longitude\":\"-87.7416\",\"offsetlat\":\"41.787498\",\"offsetlon\":\"-87.7416\",\"radius\":\"1400\",\"name\":\"Chicago Midway International Airport\",\"line1\":\"Chicago Midway International Airport\",\"line2\":\"Chicago, IL 60638\",\"line3\":null,\"line4\":\"United States\",\"house\":null,\"street\":null,\"xstreet\":null,\"unittype\":null,\"unit\":null,\"postal\":\"60638\",\"neighborhood\":null,\"city\":\"Chicago\",\"county\":\"Cook County\",\"state\":\"Illinois\",\"country\":\"United States\",\"countrycode\":\"US\",\"statecode\":\"IL\",\"countycode\":null,\"uzip\":\"60638\",\"hash\":null,\"woeid\":\"12519178\",\"woetype\":\"14\"}}}}");
    when(resource.findLocation(location, false)).thenReturn(jsonObject);
    Location loc = yqlGeolocator.locate(location, GeolocationGranularity.BROAD);
    assertThat(loc).isNotNull();
    assertThat(loc.getName()).isEqualTo(location);
    assertThat(loc.getLatitude()).isWithin(0.00001).of(41.787498);
    assertThat(loc.getLongitude()).isWithin(0.00001).of(-87.74159999999999);
  }

  @Test
  public void broadSearchWithNarrowResults() throws Exception {
    String location = "1504 North Western Avenue, Chicago";
    JSONObject jsonObject = new JSONObject("{\"query\":{\"count\":1,\"created\":\"2014-01-31T22:30:02Z\",\"lang\":\"en-US\",\"results\":{\"Result\":{\"quality\":\"62\",\"latitude\":\"41.787498\",\"longitude\":\"-87.7416\",\"offsetlat\":\"41.787498\",\"offsetlon\":\"-87.7416\",\"radius\":\"1400\",\"name\":\"Chicago Midway International Airport\",\"line1\":\"Chicago Midway International Airport\",\"line2\":\"Chicago, IL 60638\",\"line3\":null,\"line4\":\"United States\",\"house\":null,\"street\":null,\"xstreet\":null,\"unittype\":null,\"unit\":null,\"postal\":\"60638\",\"neighborhood\":null,\"city\":\"Chicago\",\"county\":\"Cook County\",\"state\":\"Illinois\",\"country\":\"United States\",\"countrycode\":\"US\",\"statecode\":\"IL\",\"countycode\":null,\"uzip\":\"60638\",\"hash\":null,\"woeid\":\"12519178\",\"woetype\":\"14\"}}}}");
    when(resource.findLocation(location, false)).thenReturn(jsonObject);
    Location loc = yqlGeolocator.locate(location, GeolocationGranularity.NARROW);
    assertThat(loc).isNull();
  }

  // The results item is not an object, but rather an array in when count is > 1
  @Test
  public void broadSearchWithMultipleItems() throws Exception {
    String location = "1504 North Western Avenue, Chicago";
    String jsonText = "{\"query\":{\"count\":13,\"created\":\"2014-02-04T06:10:15Z\",\"lang\":\"en-US\",\"results\":{\"Result\":[{\"quality\":\"62\",\"latitude\":\"41.604099\",\"longitude\":\"-88.091202\",\"offsetlat\":\"41.604099\",\"offsetlon\":\"-88.091202\",\"radius\":\"1400\",\"name\":\"Lewis University Airport\",\"line1\":\"Lewis University Airport\",\"line2\":\"Romeoville, IL 60446\",\"line3\":null,\"line4\":\"United States\",\"house\":null,\"street\":null,\"xstreet\":null,\"unittype\":null,\"unit\":null,\"postal\":\"60446\",\"neighborhood\":null,\"city\":\"Romeoville\",\"county\":\"Will County\",\"state\":\"Illinois\",\"country\":\"United States\",\"countrycode\":\"US\",\"statecode\":\"IL\",\"countycode\":null,\"uzip\":\"60446\",\"hash\":null,\"woeid\":\"12520634\",\"woetype\":\"14\"},{\"quality\":\"49\",\"latitude\":\"36.60354\",\"longitude\":\"-84.09594\",\"offsetlat\":\"36.6035\",\"offsetlon\":\"-84.09594\",\"radius\":\"700\",\"name\":null,\"line1\":null,\"line2\":\"Lot, Cumberland College, KY 40769\",\"line3\":null,\"line4\":\"United States\",\"house\":null,\"street\":null,\"xstreet\":null,\"unittype\":null,\"unit\":null,\"postal\":\"40769\",\"neighborhood\":\"Lot\",\"city\":\"Cumberland College\",\"county\":\"Whitley County\",\"state\":\"Kentucky\",\"country\":\"United States\",\"countrycode\":\"US\",\"statecode\":\"KY\",\"countycode\":null,\"uzip\":\"40769\",\"hash\":null,\"woeid\":\"2442226\",\"woetype\":\"7\"},{\"quality\":\"29\",\"latitude\":\"44.625084\",\"longitude\":\"1.59638\",\"offsetlat\":\"44.625092\",\"offsetlon\":\"1.59638\",\"radius\":\"68500\",\"name\":null,\"line1\":null,\"line2\":\"Lot\",\"line3\":null,\"line4\":\"France\",\"house\":null,\"street\":null,\"xstreet\":null,\"unittype\":null,\"unit\":null,\"postal\":null,\"neighborhood\":null,\"city\":null,\"county\":\"Lot\",\"state\":\"Midi-Pyrenees\",\"country\":\"France\",\"countrycode\":\"FR\",\"statecode\":null,\"countycode\":\"46\",\"uzip\":null,\"hash\":null,\"woeid\":\"12597172\",\"woetype\":\"9\"},{\"quality\":\"49\",\"latitude\":\"50.763146\",\"longitude\":\"4.277685\",\"offsetlat\":\"50.761009\",\"offsetlon\":\"4.27239\",\"radius\":\"1000\",\"name\":null,\"line1\":null,\"line2\":\"1651 Lot\",\"line3\":null,\"line4\":\"Belgium\",\"house\":null,\"street\":null,\"xstreet\":null,\"unittype\":null,\"unit\":null,\"postal\":\"1651\",\"neighborhood\":\"Lot\",\"city\":\"Brussels\",\"county\":\"Halle-Vilvoorde administrative Arrondissement\",\"state\":\"Vlaams Brabant\",\"country\":\"Belgium\",\"countrycode\":\"BE\",\"statecode\":\"VBR\",\"countycode\":null,\"uzip\":\"1651\",\"hash\":null,\"woeid\":\"973734\",\"woetype\":\"7\"},{\"quality\":\"49\",\"latitude\":\"59.351996\",\"longitude\":\"17.070006\",\"offsetlat\":\"59.352131\",\"offsetlon\":\"17.07041\",\"radius\":\"800\",\"name\":null,\"line1\":null,\"line2\":\"64594 Lot (Strangnas)\",\"line3\":null,\"line4\":\"Sweden\",\"house\":null,\"street\":null,\"xstreet\":null,\"unittype\":null,\"unit\":null,\"postal\":\"64594\",\"neighborhood\":\"Lot\",\"city\":\"Strangnas\",\"county\":\"Strangnas\",\"state\":\"Sodermanland\",\"country\":\"Sweden\",\"countrycode\":\"SE\",\"statecode\":null,\"countycode\":null,\"uzip\":\"64594\",\"hash\":null,\"woeid\":\"20151334\",\"woetype\":\"7\"},{\"quality\":\"49\",\"latitude\":\"56.9161\",\"longitude\":\"16.83468\",\"offsetlat\":\"56.916241\",\"offsetlon\":\"16.83461\",\"radius\":\"600\",\"name\":null,\"line1\":null,\"line2\":\"38796 Lot (Borgholm)\",\"line3\":null,\"line4\":\"Sweden\",\"house\":null,\"street\":null,\"xstreet\":null,\"unittype\":null,\"unit\":null,\"postal\":\"38796\",\"neighborhood\":\"Lot\",\"city\":\"Kopingsvik\",\"county\":\"Borgholm\",\"state\":\"Kalmar\",\"country\":\"Sweden\",\"countrycode\":\"SE\",\"statecode\":\"H\",\"countycode\":null,\"uzip\":\"38796\",\"hash\":null,\"woeid\":\"897658\",\"woetype\":\"7\"},{\"quality\":\"49\",\"latitude\":\"44.950001\",\"longitude\":\"0.76667\",\"offsetlat\":\"44.950001\",\"offsetlon\":\"0.76667\",\"radius\":\"800\",\"name\":null,\"line1\":null,\"line2\":\"24510 Lot\",\"line3\":null,\"line4\":\"France\",\"house\":null,\"street\":null,\"xstreet\":null,\"unittype\":null,\"unit\":null,\"postal\":\"24510\",\"neighborhood\":\"Lot\",\"city\":\"Ste.-Alvere\",\"county\":\"Dordogne\",\"state\":\"Aquitaine\",\"country\":\"France\",\"countrycode\":\"FR\",\"statecode\":null,\"countycode\":\"24\",\"uzip\":\"24510\",\"hash\":null,\"woeid\":\"608452\",\"woetype\":\"7\"},{\"quality\":\"49\",\"latitude\":\"57.383301\",\"longitude\":\"10.2833\",\"offsetlat\":\"57.383301\",\"offsetlon\":\"10.2833\",\"radius\":\"1000\",\"name\":null,\"line1\":null,\"line2\":\"9870 Loth\",\"line3\":null,\"line4\":\"Denmark\",\"house\":null,\"street\":null,\"xstreet\":null,\"unittype\":null,\"unit\":null,\"postal\":\"9870\",\"neighborhood\":\"Loth\",\"city\":\"Sindal\",\"county\":\"Hjorring\",\"state\":\"Nordjylland\",\"country\":\"Denmark\",\"countrycode\":\"DK\",\"statecode\":null,\"countycode\":null,\"uzip\":\"9870\",\"hash\":null,\"woeid\":\"555338\",\"woetype\":\"7\"},{\"quality\":\"49\",\"latitude\":\"59.431845\",\"longitude\":\"15.224385\",\"offsetlat\":\"59.43185\",\"offsetlon\":\"15.22439\",\"radius\":\"2000\",\"name\":null,\"line1\":null,\"line2\":\"71894 Lot (Orebro)\",\"line3\":null,\"line4\":\"Sweden\",\"house\":null,\"street\":null,\"xstreet\":null,\"unittype\":null,\"unit\":null,\"postal\":\"71894\",\"neighborhood\":\"Lot\",\"city\":\"Ervalla\",\"county\":\"Orebro\",\"state\":\"Orebro\",\"country\":\"Sweden\",\"countrycode\":\"SE\",\"statecode\":null,\"countycode\":null,\"uzip\":\"71894\",\"hash\":null,\"woeid\":\"897657\",\"woetype\":\"7\"},{\"quality\":\"49\",\"latitude\":\"59.860276\",\"longitude\":\"17.932736\",\"offsetlat\":\"59.860279\",\"offsetlon\":\"17.932739\",\"radius\":\"2100\",\"name\":null,\"line1\":null,\"line2\":\"75597 Lot (Uppsala)\",\"line3\":null,\"line4\":\"Sweden\",\"house\":null,\"street\":null,\"xstreet\":null,\"unittype\":null,\"unit\":null,\"postal\":\"75597\",\"neighborhood\":\"Lot\",\"city\":\"Uppsala\",\"county\":\"Uppsala\",\"state\":\"Uppsala Lan\",\"country\":\"Sweden\",\"countrycode\":\"SE\",\"statecode\":null,\"countycode\":\"C\",\"uzip\":\"75597\",\"hash\":null,\"woeid\":\"897659\",\"woetype\":\"7\"},{\"quality\":\"49\",\"latitude\":\"59.568455\",\"longitude\":\"17.319825\",\"offsetlat\":\"59.568459\",\"offsetlon\":\"17.31983\",\"radius\":\"2000\",\"name\":null,\"line1\":null,\"line2\":\"74599 Lot (Enkoping)\",\"line3\":null,\"line4\":\"Sweden\",\"house\":null,\"street\":null,\"xstreet\":null,\"unittype\":null,\"unit\":null,\"postal\":\"74599\",\"neighborhood\":\"Lot\",\"city\":\"Enkoping\",\"county\":\"Enkoping\",\"state\":\"Uppsala Lan\",\"country\":\"Sweden\",\"countrycode\":\"SE\",\"statecode\":null,\"countycode\":null,\"uzip\":\"74599\",\"hash\":null,\"woeid\":\"897656\",\"woetype\":\"7\"},{\"quality\":\"49\",\"latitude\":\"45.929374\",\"longitude\":\"12.213235\",\"offsetlat\":\"45.929379\",\"offsetlon\":\"12.21324\",\"radius\":\"1500\",\"name\":null,\"line1\":null,\"line2\":\"31020 Lot TV\",\"line3\":null,\"line4\":\"Italy\",\"house\":null,\"street\":null,\"xstreet\":null,\"unittype\":null,\"unit\":null,\"postal\":\"31020\",\"neighborhood\":\"Lot\",\"city\":\"San Vendemiano\",\"county\":\"Treviso\",\"state\":\"Veneto\",\"country\":\"Italy\",\"countrycode\":\"IT\",\"statecode\":null,\"countycode\":\"TV\",\"uzip\":\"31020\",\"hash\":null,\"woeid\":\"90105222\",\"woetype\":\"7\"},{\"quality\":\"49\",\"latitude\":\"48.062265\",\"longitude\":\"18.331875\",\"offsetlat\":\"48.060101\",\"offsetlon\":\"18.33498\",\"radius\":\"1600\",\"name\":null,\"line1\":null,\"line2\":\"941 42 Vel'Ke Lovce\",\"line3\":null,\"line4\":\"Slovakia\",\"house\":null,\"street\":null,\"xstreet\":null,\"unittype\":null,\"unit\":null,\"postal\":\"941 42\",\"neighborhood\":\"Vel'Ke Lovce\",\"city\":\"A Urany\",\"county\":\"Nove Zamky\",\"state\":\"Nyitra County\",\"country\":\"Slovakia\",\"countrycode\":\"SK\",\"statecode\":null,\"countycode\":null,\"uzip\":\"941 42\",\"hash\":null,\"woeid\":\"822962\",\"woetype\":\"7\"}]}}}\n";
    JSONObject jsonObject = new JSONObject(jsonText);
    when(resource.findLocation(location, false)).thenReturn(jsonObject);
    Location loc = yqlGeolocator.locate(location, GeolocationGranularity.BROAD);
    assertThat(loc).isNotNull();
    assertThat(loc.getName()).isEqualTo(location);
    assertThat(loc.getLatitude()).isWithin(0.00001).of(41.604099);
    assertThat(loc.getLongitude()).isWithin(0.00001).of(-88.091202);
  }

  // The results item is not an object, but rather an array in when count is > 1
  @Test
  public void narrowSearchWithMismatchIntersection() throws Exception {
    String location = "Chicago and Paczki, Chicago, IL";
    String jsonText = "{\"query\":{\"count\":1,\"created\":\"2014-03-03T22:45:02Z\",\"lang\":\"en-US\",\"results\":{\"Result\":{\"quality\":\"80\",\"latitude\":\"41.89669\",\"longitude\":\"-87.628212\",\"offsetlat\":\"41.89669\",\"offsetlon\":\"-87.628212\",\"radius\":\"1400\",\"name\":null,\"line1\":\"E Chicago Ave & W Chicago Ave\",\"line2\":\"Chicago, IL 60654\",\"line3\":null,\"line4\":\"United States\",\"house\":null,\"street\":\"E Chicago Ave\",\"xstreet\":\"W Chicago Ave\",\"unittype\":null,\"unit\":null,\"postal\":\"60654\",\"neighborhood\":null,\"city\":\"Chicago\",\"county\":\"Cook County\",\"state\":\"Illinois\",\"country\":\"United States\",\"countrycode\":\"US\",\"statecode\":\"IL\",\"countycode\":null,\"uzip\":\"60654\",\"hash\":null,\"woeid\":\"12784303\",\"woetype\":\"11\"}}}} \n" +
        "{\"query\":{\"count\":1,\"created\":\"2014-03-03T22:45:02Z\",\"lang\":\"en-US\",\"results\":{\"Result\":{\"quality\":\"80\",\"latitude\":\"41.89669\",\"longitude\":\"-87.628212\",\"offsetlat\":\"41.89669\",\"offsetlon\":\"-87.628212\",\"radius\":\"1400\",\"name\":null,\"line1\":\"E Chicago Ave & W Chicago Ave\",\"line2\":\"Chicago, IL 60654\",\"line3\":null,\"line4\":\"United States\",\"house\":null,\"street\":\"E Chicago Ave\",\"xstreet\":\"W Chicago Ave\",\"unittype\":null,\"unit\":null,\"postal\":\"60654\",\"neighborhood\":null,\"city\":\"Chicago\",\"county\":\"Cook County\",\"state\":\"Illinois\",\"country\":\"United States\",\"countrycode\":\"US\",\"statecode\":\"IL\",\"countycode\":null,\"uzip\":\"60654\",\"hash\":null,\"woeid\":\"12784303\",\"woetype\":\"11\"}}}}";
    JSONObject jsonObject = new JSONObject(jsonText);
    when(resource.findLocation(location, false)).thenReturn(jsonObject);
    Location loc = yqlGeolocator.locate(location, GeolocationGranularity.NARROW);
    assertThat(loc).isNull();
  }
}
