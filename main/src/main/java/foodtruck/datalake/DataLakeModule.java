package foodtruck.datalake;

import com.google.inject.AbstractModule;

/**
 * @author aviolette
 * @since 2018-12-29
 */
public class DataLakeModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(DataExportService.class).to(DataExportServiceImpl.class);
  }
}
