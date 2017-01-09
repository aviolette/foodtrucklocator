package foodtruck.server.dashboard;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import com.amazon.speech.json.SpeechletResponseEnvelope;
import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.Speechlet;
import com.amazon.speech.speechlet.SpeechletException;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import foodtruck.alexa.IntentProcessor;
import foodtruck.time.Clock;

/**
 * @author aviolette
 * @since 9/18/16
 */
@Singleton
public class AlexaTestServlet extends HttpServlet {
  private static final String JSP_PATH = "/WEB-INF/jsp/dashboard/alexaTest.jsp";
  private final Map<String, IntentProcessor> processors;
  private final Speechlet speechlet;
  private final Clock clock;

  @Inject
  public AlexaTestServlet(Map<String, IntentProcessor> processors, Speechlet speechlet, Clock clock) {
    this.processors = processors;
    this.clock = clock;
    this.speechlet = speechlet;
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    JSONObject intentObj = new JSONObject();
    for (Map.Entry<String, IntentProcessor> processorEntry : processors.entrySet()) {
      try {
        intentObj.put(processorEntry.getKey(), processorEntry.getValue()
            .getSlotNames());
      } catch (JSONException e) {
        throw new ServletException(e);
      }
    }
    req.setAttribute("intents", intentObj);
    req.setAttribute("tab", "alexa");
    req.getRequestDispatcher(JSP_PATH)
        .forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    final String json = new String(ByteStreams.toByteArray(req.getInputStream()));
    try {
      JSONObject jsonPayload = new JSONObject(json);
      Intent intent = buildIntent(jsonPayload);
      SpeechletResponse response = speechlet.onIntent(IntentRequest.builder()
          .withIntent(intent)
          .withRequestId("dummy")
          .withTimestamp(clock.now()
              .toDate())
          .build(), null);
      SpeechletResponseEnvelope envelope = new SpeechletResponseEnvelope();
      envelope.setResponse(response);
      envelope.setSessionAttributes(Maps.<String, Object>newHashMap());
      envelope.setVersion("unknown");
      resp.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
      resp.getOutputStream()
          .print(envelope.toJsonString());
    } catch (JSONException | SpeechletException e) {
      throw new ServletException(e);
    }
  }

  private Intent buildIntent(JSONObject jsonPayload) throws JSONException {
    Map<String, Slot> slots = Maps.newHashMap();
    JSONArray arr = jsonPayload.getJSONArray("slots");
    for (int i = 0; i < arr.length(); i++) {
      JSONObject obj = arr.getJSONObject(i);
      String name = obj.getString("name");
      slots.put(name, Slot.builder()
          .withName(name)
          .withValue(obj.optString("value"))
          .build());
    }
    return Intent.builder()
        .withName(jsonPayload.getString("intent"))
        .withSlots(slots)
        .build();
  }
}