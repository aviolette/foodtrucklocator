package foodtruck.alexa;

import com.amazon.speech.ui.OutputSpeech;
import com.amazon.speech.ui.PlainTextOutputSpeech;
import com.amazon.speech.ui.SsmlOutputSpeech;
import com.google.common.truth.StringSubject;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author aviolette
 * @since 9/1/16
 */
public class AlexaTestingUtils {
  public static StringSubject assertSpeech(OutputSpeech speech) {
    return (speech instanceof PlainTextOutputSpeech) ? assertThat(
        ((PlainTextOutputSpeech) speech).getText()) : assertThat(((SsmlOutputSpeech) speech).getSsml());
  }
}
