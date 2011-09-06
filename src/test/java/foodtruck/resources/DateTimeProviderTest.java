package foodtruck.resources;

import javax.annotation.Nullable;
import javax.ws.rs.core.MultivaluedMap;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.HttpRequestContext;

import org.easymock.EasyMockSupport;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author aviolette@gmail.com
 * @since 9/5/11
 */
public class DateTimeProviderTest extends EasyMockSupport {
  private DateTimeProvider provider;
  private HttpContext context;
  private HttpRequestContext request;
  private MultivaluedMap paramMap;
  private DateTimeZone zone;

  @Before
  public void before() {
    zone = DateTimeZone.forID("America/Chicago");
    provider = new DateTimeProvider(zone);
    context = createMock(HttpContext.class);
    request = createMock(HttpRequestContext.class);
    expect(context.getRequest()).andStubReturn(request);
    paramMap = createMock(MultivaluedMap.class);
    expect(request.getQueryParameters()).andStubReturn(paramMap);
  }

  @Test
  public void getInjectableShouldFindContextForDateTime() {
    replayAll();
    assertEquals(provider, provider.getInjectable(null, null, DateTime.class));
    verifyAll();
  }

  @Test
  public void getInjectableShouldReturnNullForObject() {
    replayAll();
    assertEquals(null, provider.getInjectable(null, null, Object.class));
    verifyAll();
  }

  @Test
  public void getValueShouldParseCorrectTimeString() {
    expectTime("20110905-1345");
    DateTime expected = new DateTime(2011, 9, 5, 13, 45, 0, 0, zone);
    replayAll();
    assertEquals(expected, provider.getValue(context));
    verifyAll();
  }

  @Test
  public void getValueShouldReturnNullWhenUnableToParse() {
    expectTime("20110905");
    replayAll();
    assertNull(provider.getValue(context));
    verifyAll();
  }

  @Test
  public void getValueShouldReturnNullWhenNullInput() {
    expectTime(null);
    replayAll();
    assertNull(provider.getValue(context));
    verifyAll();
  }

  private void expectTime(@Nullable String timeValue) {
    expect(paramMap.getFirst("time")).andReturn(timeValue);
  }
}
