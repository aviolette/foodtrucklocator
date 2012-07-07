// Copyright 2012 BrightTag, Inc. All rights reserved.
package foodtruck.server.resources.json;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.model.StatVector;

/**
 * @author aviolette@gmail.com
 * @since 7/6/12
 */
public class StatVectorWriter implements JSONWriter<StatVector> {
  @Override public JSONObject asJSON(StatVector statVector) throws JSONException {
    return new JSONObject();
  }
}
