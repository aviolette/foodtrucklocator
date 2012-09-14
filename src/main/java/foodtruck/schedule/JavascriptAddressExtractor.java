package foodtruck.schedule;

import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import foodtruck.dao.AddressRuleScriptDAO;
import foodtruck.model.Truck;

/**
 * An address extractor which uses an external script to match addresses.
 * @author aviolette@gmail.com
 * @since 9/13/12
 */
public class JavascriptAddressExtractor implements AddressExtractor {
  private AddressRuleScriptDAO dao;

  @Inject
  public JavascriptAddressExtractor(AddressRuleScriptDAO addressRuleScriptDAO) {
    this.dao = addressRuleScriptDAO;
  }

  @Override public List<String> parse(String tweet, Truck truck) {
    // TODO: Provide the script engine and the manager via Guice
    ScriptEngineManager mgr = new ScriptEngineManager();
    ScriptEngine jsEngine = mgr.getEngineByName("JavaScript");
    String script = dao.find().getScript();
    try {
      //TODO: sandbox
      ImmutableList.Builder<String> items = ImmutableList.builder();
      jsEngine.put("matchedItems", items);
      jsEngine.put("tweet", tweet);
      jsEngine.put("truckId", truck.getId());
      jsEngine.eval(script);
      return items.build();
    } catch (ScriptException ex) {
      throw Throwables.propagate(ex);
    }
  }
}
