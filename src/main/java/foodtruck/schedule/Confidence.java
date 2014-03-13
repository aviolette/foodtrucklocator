package foodtruck.schedule;

/**
 * The level of confidence that the result is accurate
 * @author aviolette@gmail.com
 * @since 9/19/11
 */
public enum Confidence {
  LOW {
    @Override public Confidence up() {
      return MEDIUM;
    }
  }, MEDIUM {
    @Override public Confidence up() {
      return HIGH;
    }
  }, HIGH;

  public Confidence up() {
    return this;
  }
}
