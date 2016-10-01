package foodtruck.model;

import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;

import org.joda.time.DateTime;

/**
 * @author aviolette
 * @since 9/16/16
 */
public class AlexaExchange extends ModelEntity {
  private ImmutableMap<String, String> slots;
  private String intentName;
  private boolean sessionEnded;
  private DateTime requestTime;
  private DateTime completeTime;
  private boolean hadReprompt;
  private boolean hadCard;

  private AlexaExchange(Builder builder) {
    super(builder.key);
    this.slots = builder.slots;
    this.intentName = builder.intentName;
    this.sessionEnded = builder.sessionEnded;
    this.requestTime = builder.requestTime;
    this.completeTime = builder.completeTime;
    this.hadCard = builder.hadCard;
    this.hadReprompt = builder.hadReprompt;
  }

  public static Builder builder() {
    return new Builder();
  }

  public ImmutableMap<String, String> getSlots() {
    return slots;
  }

  public ImmutableCollection<String> getSlotEntries() {
    return ImmutableSortedMap.copyOf(slots)
        .values();
  }

  public String getIntentName() {
    return intentName;
  }

  public boolean isSessionEnded() {
    return sessionEnded;
  }

  public DateTime getRequestTime() {
    return requestTime;
  }

  public DateTime getCompleteTime() {
    return completeTime;
  }

  public boolean getHadCard() {
    return hadCard;
  }

  public boolean getHadReprompt() {
    return hadReprompt;
  }

  public static class Builder {
    private Long key = -1L;
    private String intentName;
    private ImmutableMap<String, String> slots = ImmutableMap.of();
    private boolean sessionEnded;
    private DateTime requestTime;
    private DateTime completeTime;
    private boolean hadReprompt;
    private boolean hadCard;

    public Builder() {
    }

    public Builder(AlexaExchange exchange) {
      this.key = (Long) exchange.key;
      this.intentName = exchange.intentName;
      this.slots = exchange.slots;
      this.sessionEnded = exchange.sessionEnded;
      this.completeTime = exchange.completeTime;
    }

    public Builder key(long key) {
      this.key = key;
      return this;
    }

    public Builder intent(IntentRequest intent) {
      intentName = intent.getIntent()
          .getName();
      ImmutableMap.Builder<String, String> slotBuilder = ImmutableMap.builder();
      for (Slot slot : intent.getIntent()
          .getSlots()
          .values()) {
        slotBuilder.put(slot.getName(), Strings.nullToEmpty(slot.getValue()));
      }
      slots = slotBuilder.build();
      requestTime = new DateTime(intent.getTimestamp());
      return this;
    }

    public Builder slots(ImmutableMap<String, String> slots) {
      this.slots = slots;
      return this;
    }

    public Builder intentName(String intentName) {
      this.intentName = intentName;
      return this;
    }

    public Builder completeTime(DateTime completeTime) {
      this.completeTime = completeTime;
      return this;
    }

    public AlexaExchange build() {
      return new AlexaExchange(this);
    }

    public Builder requested(DateTime requested) {
      this.requestTime = requested;
      return this;
    }

    public Builder hadCard(boolean hadCard) {
      this.hadCard = hadCard;
      return this;
    }

    public Builder hadReprompt(boolean hadReprompt) {
      this.hadReprompt = hadReprompt;
      return this;
    }

    public Builder response(SpeechletResponse response) {
      this.hadReprompt = response.getReprompt() != null;
      this.hadCard = response.getCard() != null;
      this.sessionEnded = response.getShouldEndSession();
      return this;
    }
  }
}
