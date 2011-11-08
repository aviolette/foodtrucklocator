package foodtruck.config;

import java.io.FileNotFoundException;

import org.easymock.EasyMockSupport;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;

/**
 * @author aviolette@gmail.com
 * @since Jul 19, 2011
 */
public class TruckConfigParserImplTest extends EasyMockSupport {
  private TruckConfigParserImpl parser;

  @Before
  public void before() {
    final DateTimeZone dateTimeZone = DateTimeZone.forID("America/Chicago");
    parser = new TruckConfigParserImpl();
  }

  @Test
  public void testLoadSample() throws FileNotFoundException {
    String url =
        Thread.currentThread().getContextClassLoader().getResource("trucks.sample.yaml").getFile();
    replayAll();
    verifyAll();
  }

  @Test(expected = FileNotFoundException.class)
  public void testBadFileThrowsException() throws FileNotFoundException {
    replayAll();
    parser.parse("foobar.bar");
    verifyAll();
  }
}