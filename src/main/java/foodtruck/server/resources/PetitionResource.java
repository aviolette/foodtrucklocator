package foodtruck.server.resources;

import java.io.IOException;
import java.io.StringWriter;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import com.google.common.base.Throwables;
import com.google.inject.Inject;

import org.joda.time.format.DateTimeFormatter;

import au.com.bytecode.opencsv.CSVWriter;
import foodtruck.dao.PetitionSignatureDAO;
import foodtruck.model.PetitionSignature;
import foodtruck.util.FriendlyDateOnlyFormat;
import static foodtruck.server.resources.Resources.requiresAdmin;

/**
 * @author aviolette
 * @since 8/20/14
 */
@Path("/petitions/{id}.csv")
public class PetitionResource {
  private final PetitionSignatureDAO dao;
  private static final String[] HEADER =
      new String[] {"FIRST NAME", "LAST NAME", "IN DISTRICT", "SIGNED"};
  private final DateTimeFormatter formatter;

  @Inject
  public PetitionResource(PetitionSignatureDAO dao, @FriendlyDateOnlyFormat DateTimeFormatter formatter) {
    this.dao = dao;
    this.formatter = formatter;
  }

  @GET @Produces("text/csv")
  public String findAll(@PathParam("id") String id) {
    requiresAdmin();
    try {
      return response(dao.findAll());
    } catch (IOException e) {
      throw Throwables.propagate(e);
    }
  }

  public String response(Iterable<PetitionSignature> signatures) throws IOException {
    final StringWriter stringWriter = new StringWriter();
    CSVWriter writer = new CSVWriter(stringWriter);
    writer.writeNext(HEADER);
    for (PetitionSignature signature : signatures) {
      if (!signature.isValid()) {
        continue;
      }
      writer.writeNext(petitionEntry(signature));
    }
    writer.close();
    return stringWriter.getBuffer().toString();
  }

  private String[] petitionEntry(PetitionSignature signature) {
    return new String[] { signature.getFirstName(), signature.getLastName(), String.valueOf(signature.isInWard()),
        formatter.print(signature.getSigned()) };
  }

}
