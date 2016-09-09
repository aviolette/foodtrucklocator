package foodtruck.alexa;

import com.amazon.speech.ui.Card;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * @author aviolette
 * @since 9/8/16
 */
@JsonTypeName("ImageCard")
public class ImageCard extends Card {
  private String text;
  private ImageSet image;

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public ImageSet getImage() {
    return image;
  }

  public void setImage(ImageSet image) {
    this.image = image;
  }
}
