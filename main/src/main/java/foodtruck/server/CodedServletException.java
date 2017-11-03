package foodtruck.server;

import java.util.function.Supplier;

import javax.servlet.ServletException;

/**
 * An exception that contains a status code that will be conveyed to the http response.
 * @author aviolette
 * @since 10/26/17
 */
public class CodedServletException extends ServletException {

  public static final Supplier<CodedServletException> NOT_FOUND = () -> new CodedServletException(404);
  private final int code;

  public CodedServletException(int code) {
    this(code, "");
  }

  public CodedServletException(int code, String message) {
    super(message);
    this.code = code;
  }

  public int getCode() {
    return code;
  }
}
