package foodtruck.tld;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import com.google.inject.Injector;

import foodtruck.dao.TruckDAO;
import foodtruck.model.Truck;

/**
 * @author aviolette
 * @since 7/30/16
 */
public class TruckFormatTag extends TagSupport {
  private TruckDAO truckDAO;
  private boolean admin;
  private String truckId;

  public void setAdmin(boolean admin) {
    this.admin = admin;
  }

  public void setTruckId(String truckId) {
    this.truckId = truckId;
  }

  @Override
  public void setPageContext(PageContext pageContext) {
    Injector injector = (Injector) pageContext.getServletContext().getAttribute(Injector.class.getName());
    truckDAO = injector.getInstance(TruckDAO.class);
    super.setPageContext(pageContext);
  }

  @Override public int doStartTag() throws JspException {
    JspWriter out = pageContext.getOut();
    try {
      if (truckId != null) {
        String root = admin ? "/admin" : "/";
        Truck truck = truckDAO.findById(truckId);
        if (truck != null) {
          out.println("<a href='" + root + "/trucks/" + truck.getId() + "'>" + truck.getName() + "</a>");
        }
      }
    } catch (Exception e) {
      //throw Throwables.propagate(e);
    }
    return SKIP_BODY;
  }


}
