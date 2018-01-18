package foodtruck.alexa;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.amazon.speech.json.SpeechletRequestEnvelope;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.speechlet.SpeechletV2;
import com.amazon.speech.speechlet.interfaces.system.SystemInterface;
import com.amazon.speech.speechlet.interfaces.system.SystemState;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;

import org.joda.time.DateTime;

import foodtruck.dao.AlexaExchangeDAO;
import foodtruck.model.AlexaExchange;
import foodtruck.monitoring.CounterPublisher;
import foodtruck.time.Clock;

/**
 * @author aviolette
 * @since 8/25/16
 */
class FTFSpeechlet implements SpeechletV2 {
  private static final Logger log = Logger.getLogger(FTFSpeechlet.class.getName());

  private final Map<String, IntentProcessor> processors;
  private final Clock clock;
  private final AlexaExchangeDAO alexaExchangeDAO;
  private final CounterPublisher publisher;

  @Inject
  public FTFSpeechlet(Map<String, IntentProcessor> processors, Clock clock, AlexaExchangeDAO alexaExchangeDAO,
      CounterPublisher publisher) {
    this.processors = processors;
    this.clock = clock;
    this.alexaExchangeDAO = alexaExchangeDAO;
    this.publisher = publisher;
  }

  @Override
  public void onSessionStarted(
      SpeechletRequestEnvelope<SessionStartedRequest> speechletRequestEnvelope) {
    log.info("Alexa session started: " + speechletRequestEnvelope.getSession().getSessionId());
  }

  @Override
  public SpeechletResponse onLaunch(
      SpeechletRequestEnvelope<LaunchRequest> speechletRequestEnvelope) {
    log.log(Level.INFO, "Alexa launched: {0}", speechletRequestEnvelope.getSession().getSessionId());
    publisher.increment("alexa_launch");
    return SpeechletResponseBuilder.builder()
        .speechText(
            "Food Truck Finder.  You can ask me what food trucks are at a specific location or information about a specific food truck. What would you like to find?")
        .repromptText("With Food Truck Finder you can find out where individual food trucks are, or find out what " +
            "food trucks will be at a specific location.  For example, you can say What food trucks " +
            "are on Wacker today.  What truck or location would you like to know about?")
        .ask();
  }

  @Override
  public SpeechletResponse onIntent(
      SpeechletRequestEnvelope<IntentRequest> speechletRequestEnvelope) {
    IntentRequest intentRequest = speechletRequestEnvelope.getRequest();
    log.log(Level.INFO, "Received intent {0} ", AlexaUtils.intentToString(intentRequest.getIntent()));
    IntentProcessor processor = processors.get(intentRequest.getIntent().getName());
    if (processor == null) {
      log.log(Level.WARNING, "Invalid intent requested: {0}", intentRequest.getIntent()
          .getName());
      throw new RuntimeException("Invalid intent: " + intentRequest.getIntent().getName());
    }
    try {
      SystemState systemState = speechletRequestEnvelope.getContext().getState(SystemInterface.class, SystemInterface.STATE_TYPE);
      if (systemState != null) {
        log.log(Level.INFO, "State: {0} endpoint: {1}",
            new Object[]{systemState.getApiAccessToken(), systemState.getApiEndpoint()});
      } else {
        log.log(Level.INFO, "System state not specified in request");
      }
      SpeechletResponse response = processor.process(intentRequest.getIntent(),
          speechletRequestEnvelope.getSession(), speechletRequestEnvelope.getContext());
      record(intentRequest, response);
      log.log(Level.INFO, "Response {0}", AlexaUtils.speechletResponseToString(response));
      return response;
    } catch (Exception e) {
      log.log(Level.SEVERE, e.getMessage(), e);
      throw new RuntimeException(e);
    }
  }

  @Override
  public void onSessionEnded(
      SpeechletRequestEnvelope<SessionEndedRequest> speechletRequestEnvelope) {
    log.fine("Alexa session ended: " + speechletRequestEnvelope.getSession().getSessionId());
  }

  private void record(IntentRequest intentRequest, SpeechletResponse response) {
    ImmutableMap.Builder<String, String> slotBuilder = ImmutableMap.builder();
    for (Slot slot : intentRequest.getIntent()
        .getSlots()
        .values()) {
      slotBuilder.put(slot.getName(), Strings.nullToEmpty(slot.getValue()));
    }

    AlexaExchange exchange = AlexaExchange.builder()
        .intentName(intentRequest.getIntent()
            .getName())
        .slots(slotBuilder.build())
        .hadReprompt(response.getReprompt() != null)
        .hadCard(response.getCard() != null)
        .sessionEnded(response.getShouldEndSession())
        .requestTime(new DateTime(intentRequest.getTimestamp()))
        .completeTime(clock.now())
        .build();
    alexaExchangeDAO.save(exchange);
    publisher.increment("alexa_intent_" + intentRequest.getIntent()
        .getName());
  }
}
