package foodtruck.model;

import java.util.Set;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;

/**
 * Static information about a food truck.
 * @author aviolette@gmail.com
 * @since Jul 12, 2011
 */
@XmlRootElement
public class Truck {
  private String id;
  private String name;
  private String twitterHandle;
  private String url;
  private String iconUrl;
  private Set<String> categories;
  private String description;

  // for JAXB
  public Truck() {
  }

  ;

  private Truck(Builder builder) {
    this.id = builder.id;
    this.name = builder.name;
    this.twitterHandle = builder.twitter;
    this.url = builder.url;
    this.iconUrl = builder.iconUrl;
    this.categories = builder.categories;
    this.description = builder.description;

  }

  @XmlElement
  public String getName() {
    return name;
  }

  @XmlElement
  public Set<String> getCategories() {
    return categories;
  }

  @XmlElement
  public @Nullable String getDescription() {
    return description;
  }

  @XmlElement
  public String getTwitterHandle() {
    return twitterHandle;
  }

  @XmlElement
  public String getUrl() {
    return url;
  }

  @XmlElement
  public String getIconUrl() {
    return iconUrl;
  }

  @XmlElement
  public String getId() {
    return id;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id, name, url, iconUrl, twitterHandle);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (o == null || !(o instanceof Truck)) {
      return false;
    }
    Truck truck = (Truck) o;
    return id.equals(truck.id) && name.equals(truck.name) && iconUrl.equals(truck.iconUrl) &&
        Objects.equal(twitterHandle, truck.twitterHandle) && Objects.equal(url, truck.url);
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("id", id)
        .add("name", name)
        .add("url", url)
        .add("iconUrl", iconUrl)
        .add("twitterHandle", twitterHandle)
        .toString();
  }

  public static class Builder {
    private String id;
    private String name;
    private @Nullable String url;
    private String iconUrl;
    private @Nullable String twitter;
    public Set<String> categories = ImmutableSet.of();
    public String description;

    public Builder() {
    }

    public Builder id(String id) {
      this.id = id;
      return this;
    }

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder url(@Nullable String url) {
      this.url = url;
      return this;
    }

    public Builder iconUrl(String iconUrl) {
      this.iconUrl = iconUrl;
      return this;
    }

    public Builder categories(ImmutableSet<String> categories) {
      this.categories = categories;
      return this;
    }

    public Builder description(@Nullable String description) {
      this.description = description;
      return this;
    }

    public Builder twitterHandle(@Nullable String twitter) {
      this.twitter = twitter;
      return this;
    }

    public Truck build() {
      return new Truck(this);
    }
  }
}
