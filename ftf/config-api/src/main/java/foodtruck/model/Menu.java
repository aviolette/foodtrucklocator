package foodtruck.model;

import com.google.common.base.Strings;
import com.google.common.escape.Escaper;
import com.google.common.escape.Escapers;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * @author aviolette
 * @since 8/17/16
 */
public class Menu extends ModelEntity {
  private static final Escaper JS_ESCAPER = Escapers.builder().addEscape('<', "&lt;").addEscape('>', "&gt;").build();

  private final String truckId, payload;

  private Menu(Builder builder) {
    super(builder.key);
    this.truckId = builder.truckId;
    this.payload = builder.payload;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static Builder builder(Menu menu) {
    return menu == null ? builder() : new Builder(menu);
  }

  private static JSONObject scrub(JSONObject payload) throws JSONException {
    JSONArray sections = payload.getJSONArray("sections");
    for (int i = 0; i < sections.length(); i++) {
      JSONObject section = sections.getJSONObject(i);
      section.put("section", JS_ESCAPER.escape(section.getString("section")));
      String description = Strings.nullToEmpty(section.optString("description"));
      section.put("description", JS_ESCAPER.escape(description));
      JSONArray items = section.getJSONArray("items");
      for (int j = 0; j < items.length(); j++) {
        JSONObject item = items.getJSONObject(j);
        item.put("name", JS_ESCAPER.escape(item.getString("name")));
        item.put("description", JS_ESCAPER.escape(Strings.nullToEmpty(item.optString("description"))));
      }
    }
    return payload;
  }

  public String getTruckId() {
    return truckId;
  }

  public String getPayload() {
    return payload;
  }

  public String getScrubbedPayload() throws JSONException {
    return scrub(new JSONObject(payload)).toString();
  }

  public static class Builder {
    private String truckId, payload;
    private Object key;

    public Builder() {
    }

    public Builder(Menu menu) {
      this.key = menu.key;
      this.truckId = menu.truckId;
      this.payload = menu.payload;
    }

    public Builder truckId(String truckId) {
      this.truckId = truckId;
      return this;
    }

    public Builder key(Object key) {
      this.key = key;
      return this;
    }

    public Builder payload(String payload) {
      this.payload = payload;
      return this;
    }

    public Menu build() {
      return new Menu(this);
    }
  }
}
