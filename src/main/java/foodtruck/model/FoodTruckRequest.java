package foodtruck.model;

import javax.annotation.Nullable;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

import org.joda.time.DateTime;

import static com.google.common.base.Preconditions.checkState;

/**
 * @author aviolette
 * @since 12/24/13
 */
public class FoodTruckRequest extends ModelEntity {
  private final DateTime startTime;
  private final DateTime endTime;
  private final String requester;
  private final String description;
  private final Location location;
  private final String userId;
  private final String email;
  private final String phone;
  private final int expectedGuests;
  private final boolean prepaid;
  private final String eventName;
  private final boolean archived;
  private final DateTime submitted;
  private final @Nullable DateTime approved;
  private final @Nullable DateTime verified;
  private final String authCode;

  private FoodTruckRequest(Builder builder) {
    super(builder.key);
    this.startTime = builder.startTime;
    this.endTime = builder.endTime;
    this.requester = builder.requester;
    this.description = builder.description;
    this.location = builder.location;
    this.email = builder.email;
    this.phone = builder.phone;
    this.userId = builder.userId;
    this.prepaid = builder.prepaid;
    this.expectedGuests = builder.expectedGuests;
    this.eventName = builder.eventName;
    this.archived = builder.archived;
    this.submitted = builder.submitted;
    this.approved = builder.approved;
    this.verified = builder.verified;
    this.authCode = builder.authCode;
  }

  @Override public void validate() throws IllegalStateException {
    super.validate();
    checkState(startTime != null, "Start time cannot be null");
    checkState(endTime != null && !endTime.isBefore(startTime), "End time cannot be before start time");
    checkState(!Strings.isNullOrEmpty(requester), "Requester not specified.");
    checkState(location != null, "Location not specified.");
    checkState(!Strings.isNullOrEmpty(email), "Email not specified.");
  }

  @Nullable public DateTime getVerified() {
    return verified;
  }

  public String getAuthCode() {
    return authCode;
  }

  public boolean isArchived() {
    return archived;
  }

  public DateTime getSubmitted() {
    return submitted;
  }

  @Nullable public DateTime getApproved() {
    return approved;
  }

  public String getEventName() {
    return this.eventName;
  }

  public boolean isPrepaid() {
    return this.prepaid;
  }

  public int getExpectedGuests() {
    return this.expectedGuests;
  }

  public String getUserId() {
    return userId;
  }

  public DateTime getStartTime() {
    return startTime;
  }

  public DateTime getEndTime() {
    return endTime;
  }

  public String getRequester() {
    return requester;
  }

  public String getDescription() {
    return description;
  }

  public Location getLocation() {
    return location;
  }

  public String getEmail() {
    return email;
  }

  public String getPhone() {
    return phone;
  }

  public static FoodTruckRequest.Builder builder() {
    return new Builder();
  }

  public static Builder copy(FoodTruckRequest request) {
    return new Builder(request);
  }

  @Override public boolean equals(Object o) {
    return super.equals(o);
  }

  @Override public int hashCode() {
    return Objects.hashCode(getKey(), requester, email, startTime, location);
  }

  @Override public String toString() {
    return super.toString();
  }

  public static Builder builder(FoodTruckRequest request) {
    return new Builder(request);
  }

  public static class Builder {
    private String eventName;
    private DateTime startTime;
    private DateTime endTime;
    private String requester;
    private String description;
    private Location location;
    private String email;
    private String phone;
    private String userId;
    private Object key;
    private int expectedGuests;
    public boolean prepaid;
    private boolean archived;
    private DateTime submitted;
    private @Nullable DateTime approved;
    private @Nullable DateTime verified;
    private String authCode;


    public Builder() {}

    public Builder(FoodTruckRequest request) {
      eventName = request.getEventName();
      startTime = request.getStartTime();
      endTime = request.getEndTime();
      requester = request.getRequester();
      description = request.getDescription();
      location = request.getLocation();
      email = request.getEmail();
      phone = request.getPhone();
      userId = request.getUserId();
      key = request.getKey();
      prepaid = request.isPrepaid();
      expectedGuests = request.getExpectedGuests();
      archived = request.archived;
      submitted = request.submitted;
      approved = request.approved;
      verified = request.getVerified();
      authCode = request.getAuthCode();
    }

    public Builder key(Object key) {
      this.key = key;
      return this;
    }

    public Builder verified(@Nullable DateTime verified) {
      this.verified = verified;
      return this;
    }

    public Builder authCode(String authCode) {
      this.authCode = authCode;
      return this;
    }

    public Builder archived(boolean archived) {
      this.archived = archived;
      return this;
    }

    public Builder submitted(DateTime submitted) {
      this.submitted = submitted;
      return this;
    }

    public Builder approved(DateTime approved) {
      this.approved = approved;
      return this;
    }

    public Builder eventName(String eventName) {
      this.eventName = eventName;
      return this;
    }

    public Builder prepaid(boolean prepaid) {
      this.prepaid = prepaid;
      return this;
    }

    public Builder expectedGuests(int expectedGuests) {
      this.expectedGuests = expectedGuests;
      return this;
    }

    public Builder email(String email) {
      this.email = email;
      return this;
    }

    public Builder phone(String phone) {
      this.phone = phone;
      return this;
    }

    public Builder userId(String userId) {
      this.userId = userId;
      return this;
    }

    public Builder startTime(DateTime startTime) {
      this.startTime = startTime;
      return this;
    }

    public Builder endTime(DateTime endTime) {
      this.endTime = endTime;
      return this;
    }

    public Builder requester(String requester) {
      this.requester = requester;
      return this;
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder location(Location location) {
      this.location = location;
      return this;
    }

    public FoodTruckRequest build() {
      return new FoodTruckRequest(this);
    }
  }
}
