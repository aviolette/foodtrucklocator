package foodtruck.schedule;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

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
  private final AddressRuleScriptDAO dao;
  private final ScriptEngineManager scriptEngineManager;
  private static final Logger log = Logger.getLogger(JavascriptAddressExtractor.class.getName());

  @Inject
  public JavascriptAddressExtractor(AddressRuleScriptDAO addressRuleScriptDAO,
      ScriptEngineManager scriptEngineManager) {
    this.dao = addressRuleScriptDAO;
    this.scriptEngineManager = scriptEngineManager;
  }

  @Override public List<String> parse(String tweet, Truck truck) {
    String script = dao.find().getScript();
    try {
      log.log(Level.INFO, "Matching text {}", tweet);
      return executeScript(tweet, truck, script);
    } catch (ScriptException ex) {
      log.log(Level.SEVERE, ex.getMessage(), ex);
      return ImmutableList.of();
    }
  }

  public List<String> executeScript(String tweet, Truck truck, String script) throws ScriptException {
    ScriptEngine jsEngine = scriptEngineManager.getEngineByName("JavaScript");
    ImmutableList.Builder<String> items = ImmutableList.builder();
    jsEngine.put("matchedItems", items);
    jsEngine.put("tweet", tweet);
    jsEngine.put("truck", truck);
    jsEngine.eval(script);
    return items.build();
  }
}
