package foodtruck.schedule;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import foodtruck.geolocation.GeoLocator;
import foodtruck.geolocation.GeolocationGranularity;
import foodtruck.model.Truck;
import foodtruck.model.TruckStop;

import static com.google.common.truth.Truth.assertThat;
import static foodtruck.schedule.ModelTestHelper.clarkAndMonroe;
import static foodtruck.schedule.ModelTestHelper.wackerAndAdams;

/**
 * @author aviolette
 * @since 2/18/18
 */
@RunWith(MockitoJUnitRunner.class)
public class DrupalCalendarStopReaderTest extends Mockito {

  private @Mock GeoLocator geoLocator;

  @Test
  public void readFrom() {
    String input = "<!DOCTYPE html>\n" +
        "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" version=\"XHTML+RDFa 1.0\" dir=\"ltr\"\n" +
        "  xmlns:og=\"http://ogp.me/ns#\">\n" + "\n" +
        "<head profile=\"http://www.w3.org/1999/xhtml/vocab\">\n" +
        "  <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n" +
        "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />\n" +
        "<meta name=\"generator\" content=\"Drupal 7 (http://drupal.org)\" />\n" +
        "<link rel=\"canonical\" href=\"http://smokinbbqkitchen.com/events/calendar\" />\n" +
        "<link rel=\"shortlink\" href=\"http://smokinbbqkitchen.com/events/calendar\" />\n" +
        "  <title>Our Schedule | Smokin&#039; BBQ Kitchen</title>\n" +
        "  <link type=\"text/css\" rel=\"stylesheet\" href=\"http://smokinbbqkitchen.com/modules/system/system.base.css?obaqd3\" media=\"all\" />\n" +
        "<link type=\"text/css\" rel=\"stylesheet\" href=\"http://smokinbbqkitchen.com/modules/system/system.messages.css?obaqd3\" media=\"all\" />\n" +
        "<link type=\"text/css\" rel=\"stylesheet\" href=\"http://smokinbbqkitchen.com/modules/system/system.theme.css?obaqd3\" media=\"all\" />\n" +
        "<link type=\"text/css\" rel=\"stylesheet\" href=\"http://smokinbbqkitchen.com/profiles/restaurant/modules/contrib/panopoly_core/css/panopoly-jquery-ui-theme.css?obaqd3\" media=\"all\" />\n" +
        "<link type=\"text/css\" rel=\"stylesheet\" href=\"http://smokinbbqkitchen.com/profiles/restaurant/modules/contrib/jquery_update/replace/ui/themes/base/minified/jquery.ui.accordion.min.css?obaqd3\" media=\"all\" />\n" +
        "<link type=\"text/css\" rel=\"stylesheet\" href=\"http://smokinbbqkitchen.com/profiles/restaurant/modules/contrib/calendar/css/calendar_multiday.css?obaqd3\" media=\"all\" />\n" +
        "<link type=\"text/css\" rel=\"stylesheet\" href=\"http://smokinbbqkitchen.com/profiles/restaurant/modules/contrib/date/date_api/date.css?obaqd3\" media=\"all\" />\n" +
        "<link type=\"text/css\" rel=\"stylesheet\" href=\"http://smokinbbqkitchen.com/profiles/restaurant/modules/contrib/date/date_popup/themes/datepicker.1.7.css?obaqd3\" media=\"all\" />\n" +
        "<link type=\"text/css\" rel=\"stylesheet\" href=\"http://smokinbbqkitchen.com/profiles/restaurant/modules/contrib/fences/field.css?obaqd3\" media=\"all\" />\n" +
        "<link type=\"text/css\" rel=\"stylesheet\" href=\"http://smokinbbqkitchen.com/modules/node/node.css?obaqd3\" media=\"all\" />\n" +
        "<link type=\"text/css\" rel=\"stylesheet\" href=\"http://smokinbbqkitchen.com/profiles/restaurant/modules/contrib/panopoly_admin/panopoly-admin-navbar.css?obaqd3\" media=\"all\" />\n" +
        "<link type=\"text/css\" rel=\"stylesheet\" href=\"http://smokinbbqkitchen.com/profiles/restaurant/modules/contrib/panopoly_core/css/panopoly-dropbutton.css?obaqd3\" media=\"all\" />\n" +
        "<link type=\"text/css\" rel=\"stylesheet\" href=\"http://smokinbbqkitchen.com/profiles/restaurant/modules/contrib/panopoly_magic/css/panopoly-magic.css?obaqd3\" media=\"all\" />\n" +
        "<link type=\"text/css\" rel=\"stylesheet\" href=\"http://smokinbbqkitchen.com/profiles/restaurant/modules/contrib/panopoly_theme/css/panopoly-featured.css?obaqd3\" media=\"all\" />\n" +
        "<link type=\"text/css\" rel=\"stylesheet\" href=\"http://smokinbbqkitchen.com/profiles/restaurant/modules/contrib/panopoly_theme/css/panopoly-accordian.css?obaqd3\" media=\"all\" />\n" +
        "<link type=\"text/css\" rel=\"stylesheet\" href=\"http://smokinbbqkitchen.com/profiles/restaurant/modules/contrib/panopoly_theme/css/panopoly-layouts.css?obaqd3\" media=\"all\" />\n" +
        "<link type=\"text/css\" rel=\"stylesheet\" href=\"http://smokinbbqkitchen.com/profiles/restaurant/modules/contrib/panopoly_widgets/panopoly-widgets.css?obaqd3\" media=\"all\" />\n" +
        "<link type=\"text/css\" rel=\"stylesheet\" href=\"http://smokinbbqkitchen.com/profiles/restaurant/modules/contrib/panopoly_widgets/panopoly-widgets-spotlight.css?obaqd3\" media=\"all\" />\n" +
        "<link type=\"text/css\" rel=\"stylesheet\" href=\"http://smokinbbqkitchen.com/profiles/restaurant/modules/contrib/panopoly_wysiwyg/panopoly-wysiwyg.css?obaqd3\" media=\"all\" />\n" +
        "<link type=\"text/css\" rel=\"stylesheet\" href=\"http://smokinbbqkitchen.com/profiles/restaurant/modules/contrib/radix_layouts/radix_layouts.css?obaqd3\" media=\"all\" />\n" +
        "<link type=\"text/css\" rel=\"stylesheet\" href=\"http://smokinbbqkitchen.com/modules/search/search.css?obaqd3\" media=\"all\" />\n" +
        "<link type=\"text/css\" rel=\"stylesheet\" href=\"http://smokinbbqkitchen.com/modules/user/user.css?obaqd3\" media=\"all\" />\n" +
        "<link type=\"text/css\" rel=\"stylesheet\" href=\"http://smokinbbqkitchen.com/profiles/restaurant/modules/contrib/views/css/views.css?obaqd3\" media=\"all\" />\n" +
        "<link type=\"text/css\" rel=\"stylesheet\" href=\"http://smokinbbqkitchen.com/profiles/restaurant/modules/contrib/caption_filter/caption-filter.css?obaqd3\" media=\"all\" />\n" +
        "<link type=\"text/css\" rel=\"stylesheet\" href=\"http://smokinbbqkitchen.com/profiles/restaurant/modules/restaurant/restaurant_admin/css/restaurant_admin.css?obaqd3\" media=\"all\" />\n" +
        "<link type=\"text/css\" rel=\"stylesheet\" href=\"http://smokinbbqkitchen.com/profiles/restaurant/modules/contrib/ctools/css/ctools.css?obaqd3\" media=\"all\" />\n" +
        "<link type=\"text/css\" rel=\"stylesheet\" href=\"http://smokinbbqkitchen.com/profiles/restaurant/modules/contrib/panels/css/panels.css?obaqd3\" media=\"all\" />\n" +
        "<link type=\"text/css\" rel=\"stylesheet\" href=\"http://smokinbbqkitchen.com/profiles/restaurant/modules/contrib/date/date_views/css/date_views.css?obaqd3\" media=\"all\" />\n" +
        "<style type=\"text/css\" media=\"all\">\n" + "<!--/*--><![CDATA[/*><!--*/\n" +
        ".footer { background-image: url(\"/profiles/restaurant/themes/contrib/sizzle/assets/images/bg/bg-footer-default.jpg\") }\n" +
        "/*]]>*/-->\n" + "</style>\n" +
        "<link type=\"text/css\" rel=\"stylesheet\" href=\"http://smokinbbqkitchen.com/profiles/restaurant/themes/contrib/sizzle/assets/css/sizzle.style.css?obaqd3\" media=\"all\" />\n" +
        "<link type=\"text/css\" rel=\"stylesheet\" href=\"http://smokinbbqkitchen.com/profiles/restaurant/themes/contrib/sizzle/assets/css/sizzle.custom.css?obaqd3\" media=\"all\" />\n" +
        "<link type=\"text/css\" rel=\"stylesheet\" href=\"http://smokinbbqkitchen.com/profiles/restaurant/modules/contrib/panopoly_images/panopoly-images.css?obaqd3\" media=\"all\" />\n" +
        "  <script type=\"text/javascript\" src=\"http://smokinbbqkitchen.com/profiles/restaurant/modules/contrib/jquery_update/replace/jquery/1.7/jquery.min.js?v=1.7.2\"></script>\n" +
        "<script type=\"text/javascript\" src=\"http://smokinbbqkitchen.com/misc/jquery.once.js?v=1.2\"></script>\n" +
        "<script type=\"text/javascript\" src=\"http://smokinbbqkitchen.com/misc/drupal.js?obaqd3\"></script>\n" +
        "<script type=\"text/javascript\" src=\"http://maxcdn.bootstrapcdn.com/bootstrap/3.1.1/js/bootstrap.min.js\"></script>\n" +
        "<script type=\"text/javascript\" src=\"http://smokinbbqkitchen.com/profiles/restaurant/modules/contrib/jquery_update/replace/ui/ui/minified/jquery.ui.core.min.js?v=1.10.2\"></script>\n" +
        "<script type=\"text/javascript\" src=\"http://smokinbbqkitchen.com/profiles/restaurant/modules/contrib/jquery_update/replace/ui/ui/minified/jquery.ui.widget.min.js?v=1.10.2\"></script>\n" +
        "<script type=\"text/javascript\" src=\"http://smokinbbqkitchen.com/profiles/restaurant/modules/contrib/jquery_update/replace/ui/ui/minified/jquery.ui.tabs.min.js?v=1.10.2\"></script>\n" +
        "<script type=\"text/javascript\" src=\"http://smokinbbqkitchen.com/profiles/restaurant/modules/contrib/jquery_update/replace/ui/ui/minified/jquery.ui.accordion.min.js?v=1.10.2\"></script>\n" +
        "<script type=\"text/javascript\" src=\"http://smokinbbqkitchen.com/profiles/restaurant/modules/contrib/panopoly_images/panopoly-images.js?obaqd3\"></script>\n" +
        "<script type=\"text/javascript\" src=\"http://smokinbbqkitchen.com/profiles/restaurant/modules/contrib/panopoly_admin/panopoly-admin.js?obaqd3\"></script>\n" +
        "<script type=\"text/javascript\" src=\"http://smokinbbqkitchen.com/profiles/restaurant/modules/contrib/panopoly_magic/panopoly-magic.js?obaqd3\"></script>\n" +
        "<script type=\"text/javascript\" src=\"http://smokinbbqkitchen.com/profiles/restaurant/modules/contrib/panopoly_theme/js/panopoly-accordion.js?obaqd3\"></script>\n" +
        "<script type=\"text/javascript\" src=\"http://smokinbbqkitchen.com/profiles/restaurant/modules/contrib/caption_filter/js/caption-filter.js?obaqd3\"></script>\n" +
        "<script type=\"text/javascript\" src=\"http://smokinbbqkitchen.com/profiles/restaurant/libraries/jquery.imagesloaded/jquery.imagesloaded.min.js?obaqd3\"></script>\n" +
        "<script type=\"text/javascript\" src=\"http://smokinbbqkitchen.com/profiles/restaurant/themes/contrib/radix/assets/js/radix.script.js?obaqd3\"></script>\n" +
        "<script type=\"text/javascript\" src=\"http://smokinbbqkitchen.com/profiles/restaurant/themes/restaurant/restaurant_radix/assets/javascripts/restaurant_radix.script.js?obaqd3\"></script>\n" +
        "<script type=\"text/javascript\" src=\"http://smokinbbqkitchen.com/profiles/restaurant/themes/contrib/sizzle/assets/vendor/jquery.matchHeight/jquery.matchHeight-min.js?obaqd3\"></script>\n" +
        "<script type=\"text/javascript\" src=\"http://smokinbbqkitchen.com/profiles/restaurant/themes/contrib/sizzle/assets/vendor/waypoints/lib/jquery.waypoints.min.js?obaqd3\"></script>\n" +
        "<script type=\"text/javascript\" src=\"http://smokinbbqkitchen.com/profiles/restaurant/themes/contrib/sizzle/assets/vendor/waypoints/lib/shortcuts/sticky.min.js?obaqd3\"></script>\n" +
        "<script type=\"text/javascript\" src=\"http://smokinbbqkitchen.com/profiles/restaurant/themes/contrib/sizzle/assets/vendor/waypoints/lib/shortcuts/inview.min.js?obaqd3\"></script>\n" +
        "<script type=\"text/javascript\" src=\"http://smokinbbqkitchen.com/profiles/restaurant/themes/contrib/sizzle/assets/js/sizzle.script.js?obaqd3\"></script>\n" +
        "<script type=\"text/javascript\">\n" + "<!--//--><![CDATA[//><!--\n" +
        "jQuery.extend(Drupal.settings, {\"basePath\":\"\\/\",\"pathPrefix\":\"\",\"ajaxPageState\":{\"theme\":\"sizzle\",\"theme_token\":\"8PWocDVcIb85Wu02NQOw59UyJrfp8c61tr2LJL0IB7s\",\"js\":{\"profiles\\/restaurant\\/modules\\/contrib\\/panopoly_widgets\\/panopoly-widgets.js\":1,\"profiles\\/restaurant\\/modules\\/contrib\\/panopoly_widgets\\/panopoly-widgets-spotlight.js\":1,\"profiles\\/restaurant\\/modules\\/contrib\\/jquery_update\\/replace\\/jquery\\/1.7\\/jquery.min.js\":1,\"misc\\/jquery.once.js\":1,\"misc\\/drupal.js\":1,\"http:\\/\\/maxcdn.bootstrapcdn.com\\/bootstrap\\/3.1.1\\/js\\/bootstrap.min.js\":1,\"profiles\\/restaurant\\/modules\\/contrib\\/jquery_update\\/replace\\/ui\\/ui\\/minified\\/jquery.ui.core.min.js\":1,\"profiles\\/restaurant\\/modules\\/contrib\\/jquery_update\\/replace\\/ui\\/ui\\/minified\\/jquery.ui.widget.min.js\":1,\"profiles\\/restaurant\\/modules\\/contrib\\/jquery_update\\/replace\\/ui\\/ui\\/minified\\/jquery.ui.tabs.min.js\":1,\"profiles\\/restaurant\\/modules\\/contrib\\/jquery_update\\/replace\\/ui\\/ui\\/minified\\/jquery.ui.accordion.min.js\":1,\"profiles\\/restaurant\\/modules\\/contrib\\/panopoly_images\\/panopoly-images.js\":1,\"profiles\\/restaurant\\/modules\\/contrib\\/panopoly_admin\\/panopoly-admin.js\":1,\"profiles\\/restaurant\\/modules\\/contrib\\/panopoly_magic\\/panopoly-magic.js\":1,\"profiles\\/restaurant\\/modules\\/contrib\\/panopoly_theme\\/js\\/panopoly-accordion.js\":1,\"profiles\\/restaurant\\/modules\\/contrib\\/caption_filter\\/js\\/caption-filter.js\":1,\"profiles\\/restaurant\\/libraries\\/jquery.imagesloaded\\/jquery.imagesloaded.min.js\":1,\"profiles\\/restaurant\\/themes\\/contrib\\/radix\\/assets\\/js\\/radix.script.js\":1,\"profiles\\/restaurant\\/themes\\/restaurant\\/restaurant_radix\\/assets\\/javascripts\\/restaurant_radix.script.js\":1,\"profiles\\/restaurant\\/themes\\/contrib\\/sizzle\\/assets\\/vendor\\/jquery.matchHeight\\/jquery.matchHeight-min.js\":1,\"profiles\\/restaurant\\/themes\\/contrib\\/sizzle\\/assets\\/vendor\\/waypoints\\/lib\\/jquery.waypoints.min.js\":1,\"profiles\\/restaurant\\/themes\\/contrib\\/sizzle\\/assets\\/vendor\\/waypoints\\/lib\\/shortcuts\\/sticky.min.js\":1,\"profiles\\/restaurant\\/themes\\/contrib\\/sizzle\\/assets\\/vendor\\/waypoints\\/lib\\/shortcuts\\/inview.min.js\":1,\"profiles\\/restaurant\\/themes\\/contrib\\/sizzle\\/assets\\/js\\/sizzle.script.js\":1},\"css\":{\"modules\\/system\\/system.base.css\":1,\"modules\\/system\\/system.messages.css\":1,\"modules\\/system\\/system.theme.css\":1,\"misc\\/ui\\/jquery.ui.theme.css\":1,\"misc\\/ui\\/jquery.ui.accordion.css\":1,\"profiles\\/restaurant\\/modules\\/contrib\\/calendar\\/css\\/calendar_multiday.css\":1,\"profiles\\/restaurant\\/modules\\/contrib\\/date\\/date_api\\/date.css\":1,\"profiles\\/restaurant\\/modules\\/contrib\\/date\\/date_popup\\/themes\\/datepicker.1.7.css\":1,\"modules\\/field\\/theme\\/field.css\":1,\"modules\\/node\\/node.css\":1,\"profiles\\/restaurant\\/modules\\/contrib\\/panopoly_admin\\/panopoly-admin-navbar.css\":1,\"profiles\\/restaurant\\/modules\\/contrib\\/panopoly_core\\/css\\/panopoly-dropbutton.css\":1,\"profiles\\/restaurant\\/modules\\/contrib\\/panopoly_magic\\/css\\/panopoly-magic.css\":1,\"profiles\\/restaurant\\/modules\\/contrib\\/panopoly_theme\\/css\\/panopoly-featured.css\":1,\"profiles\\/restaurant\\/modules\\/contrib\\/panopoly_theme\\/css\\/panopoly-accordian.css\":1,\"profiles\\/restaurant\\/modules\\/contrib\\/panopoly_theme\\/css\\/panopoly-layouts.css\":1,\"profiles\\/restaurant\\/modules\\/contrib\\/panopoly_widgets\\/panopoly-widgets.css\":1,\"profiles\\/restaurant\\/modules\\/contrib\\/panopoly_widgets\\/panopoly-widgets-spotlight.css\":1,\"profiles\\/restaurant\\/modules\\/contrib\\/panopoly_wysiwyg\\/panopoly-wysiwyg.css\":1,\"profiles\\/restaurant\\/modules\\/contrib\\/radix_layouts\\/radix_layouts.css\":1,\"modules\\/search\\/search.css\":1,\"modules\\/user\\/user.css\":1,\"profiles\\/restaurant\\/modules\\/contrib\\/views\\/css\\/views.css\":1,\"profiles\\/restaurant\\/modules\\/contrib\\/caption_filter\\/caption-filter.css\":1,\"profiles\\/restaurant\\/modules\\/restaurant\\/restaurant_admin\\/css\\/restaurant_admin.css\":1,\"profiles\\/restaurant\\/modules\\/contrib\\/ctools\\/css\\/ctools.css\":1,\"profiles\\/restaurant\\/modules\\/contrib\\/panels\\/css\\/panels.css\":1,\"profiles\\/restaurant\\/modules\\/contrib\\/date\\/date_views\\/css\\/date_views.css\":1,\"0\":1,\"profiles\\/restaurant\\/themes\\/contrib\\/sizzle\\/assets\\/css\\/sizzle.style.css\":1,\"profiles\\/restaurant\\/themes\\/contrib\\/sizzle\\/assets\\/css\\/sizzle.custom.css\":1,\"profiles\\/restaurant\\/modules\\/contrib\\/panopoly_images\\/panopoly-images.css\":1}},\"CToolsModal\":{\"modalSize\":{\"type\":\"scale\",\"width\":\".9\",\"height\":\".9\",\"addWidth\":0,\"addHeight\":0,\"contentRight\":25,\"contentBottom\":75},\"modalOptions\":{\"opacity\":\".55\",\"background-color\":\"#FFF\"},\"animationSpeed\":\"fast\",\"modalTheme\":\"CToolsModalDialog\",\"throbberTheme\":\"CToolsModalThrobber\"},\"panopoly_magic\":{\"pane_add_preview_mode\":\"single\"}});\n" +
        "//--><!]]>\n" + "</script>\n" + "  <!--[if lt IE 9]>\n" + "  <script>\n" +
        "    document.createElement('header');\n" + "    document.createElement('nav');\n" +
        "    document.createElement('section');\n" + "    document.createElement('article');\n" +
        "    document.createElement('aside');\n" + "    document.createElement('footer');\n" +
        "  </script>\n" + "  <![endif]-->\n" + "</head>\n" +
        "<body class=\"html not-front not-logged-in no-sidebars page-events page-events-calendar region-content page-is-panel page-with-region-region_a page-with-region-region_b page-with-region-region_c restaurant-toolbar panel-layout-layout_events panel-region-region_a panel-region-region_b panel-region-region_c boxed\" >\n" +
        "<div id=\"skip-link\">\n" +
        "  <a href=\"#main\" class=\"element-invisible element-focusable\">Skip to main content</a>\n" +
        "</div>\n" + "<div class=\"page\">\n" +
        "  <header class=\"header padding--sm\" role=\"header\">\n" +
        "    <div class=\"container\">\n" +
        "            <div class=\"address margin--sm--top\">\n" +
        "                  <i class=\"fa fa-map-marker\"></i> Coming to a Chicagoland location near you!\n" +
        "Check our schedule page, or Contact Us to schedule a visit to \n" +
        "Your Business, Food Event or Private Party\n" +
        "                          <i class=\"fa fa-phone margin--sm--left\"></i> +1 708.228.9309              </div>\n" +
        "          </div>\n" + "  </header>\n" + "\n" +
        "  <nav class=\"navbar navbar-default border-color-primary\" role=\"navigation\">\n" +
        "    <div class=\"container\">\n" + "      <div class=\"navbar-header\">\n" +
        "        <div class=\"visible-xs pull-left\">\n" +
        "          <a href=\"/menus\" class=\"btn\">The Menu</a>                  </div>\n" +
        "        <button type=\"button\" class=\"navbar-toggle\" data-toggle=\"collapse\" data-target=\"#navbar-collapse\">\n" +
        "          <i class=\"fa fa-cutlery fa-2x\"></i>\n" + "        </button>\n" +
        "      </div>\n" + "\n" +
        "      <div class=\"collapse navbar-collapse\" id=\"navbar-collapse\">\n" +
        "                  <ul class=\"nav navbar-nav\">\n" +
        "            <li class=\"first leaf menu-link-home\"><a href=\"/home\">Home</a></li>\n" +
        "<li class=\"leaf menu-link-menu\"><a href=\"/menus\">Menu</a></li>\n" +
        "<li class=\"leaf active menu-link-schedule\"><a href=\"/events/calendar\" title=\"\" class=\"active\">Schedule</a></li>\n" +
        "<li class=\"leaf menu-link-blog\"><a href=\"/news\" title=\"\">Blog</a></li>\n" +
        "<li class=\"last leaf menu-link-about\"><a href=\"/about\">About</a></li>\n" +
        "          </ul>\n" + "              </div>\n" + "    </div>\n" + "  </nav>\n" + "\n" +
        "  <main id=\"main\" class=\"main\">\n" +
        "                <div class=\"layout layout--locations\">\n" +
        "      <div class=\"layout__region layout__region--region-a border--sm--bottom\">\n" +
        "      <div class=\"panel-pane pane-fieldable-panels-pane pane-vuuid-bab8c266-e7bb-4a69-8092-a76bd43a2130 pane-bundle-image\"  >\n" +
        "  \n" + "      \n" + "  \n" + "  <div class=\"pane-content\">\n" +
        "    <div class=\"fieldable-panels-pane\">\n" +
        "    <div class=\"field field-name-field-basic-image-image field-type-image field-label-hidden\"><div class=\"field-items\"><div class=\"field-item even\"><img class=\"panopoly-image-full\" src=\"http://smokinbbqkitchen.com/sites/default/files/styles/panopoly_image_full/public/general/small-truck_0.png?itok=HQQEGghU\" alt=\"\" /></div></div></div></div>\n" +
        "  </div>\n" + "\n" + "  \n" + "  </div>\n" + "    </div>\n" + "  \n" + "    \n" +
        "      <div class=\"layout__region layout__region--region-c container padding--lg--top padding--lg--bottom\">\n" +
        "      <div class=\"panel-pane pane-views-panes pane-events-calendar-events-calendar\"  >\n" +
        "  \n" + "      \n" + "  \n" + "  <div class=\"pane-content\">\n" +
        "    <div class=\"view view-events-calendar view-id-events_calendar view-display-id-events_calendar view-dom-id-cd047866f79ee5f1366dfb848d6c5cbd view--events-calendar view--events-calendar--events-calendar\">\n" +
        "            <div class=\"view__header\">\n" +
        "      <div class=\"date-nav-wrapper clearfix\">\n" +
        "  <h3 class=\"pull-left\">March 2018</h3>\n" + "  <div class=\"btn-group pull-right\">\n" +
        "          <a href=\"http://smokinbbqkitchen.com/events/calendar?month=2018-02\" title=\"Navigate to previous month\" rel=\"nofollow\" class=\"btn btn-default\">&laquo; Previous</a>              <a href=\"http://smokinbbqkitchen.com/events/calendar?month=2018-04\" title=\"Navigate to next month\" rel=\"nofollow\" class=\"btn btn-default\">Next &raquo;</a>      </div>\n" +
        "</div>    </div>\n" + "  \n" + "  \n" + "  \n" + "      <div class=\"view__content\">\n" +
        "      <div class=\"calendar-calendar\"><div class=\"month-view\">\n" +
        "<table class=\"full\">\n" + "  <thead>\n" + "    <tr>\n" +
        "              <th class=\"days sun\" id=\"Sunday\">\n" + "          Sun        </th>\n" +
        "              <th class=\"days mon\" id=\"Monday\">\n" + "          Mon        </th>\n" +
        "              <th class=\"days tue\" id=\"Tuesday\">\n" + "          Tue        </th>\n" +
        "              <th class=\"days wed\" id=\"Wednesday\">\n" +
        "          Wed        </th>\n" + "              <th class=\"days thu\" id=\"Thursday\">\n" +
        "          Thu        </th>\n" + "              <th class=\"days fri\" id=\"Friday\">\n" +
        "          Fri        </th>\n" + "              <th class=\"days sat\" id=\"Saturday\">\n" +
        "          Sat        </th>\n" + "          </tr>\n" + "  </thead>\n" + "  <tbody>\n" +
        "    <tr class=\"date-box\">\n" +
        "  <td id=\"events_calendar-2018-02-25-date-box\" class=\"date-box past empty\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-02-25\"  headers=\"Sunday\"  data-day-of-month=\"25\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"month day\"> 25 </div>  </div>\n" +
        "</td>\n" +
        "<td id=\"events_calendar-2018-02-26-date-box\" class=\"date-box past empty\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-02-26\"  headers=\"Monday\"  data-day-of-month=\"26\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"month day\"> 26 </div>  </div>\n" +
        "</td>\n" +
        "<td id=\"events_calendar-2018-02-27-date-box\" class=\"date-box past empty\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-02-27\"  headers=\"Tuesday\"  data-day-of-month=\"27\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"month day\"> 27 </div>  </div>\n" +
        "</td>\n" +
        "<td id=\"events_calendar-2018-02-28-date-box\" class=\"date-box past empty\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-02-28\"  headers=\"Wednesday\"  data-day-of-month=\"28\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"month day\"> 28 </div>  </div>\n" +
        "</td>\n" +
        "<td id=\"events_calendar-2018-03-01-date-box\" class=\"date-box past\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-01\"  headers=\"Thursday\"  data-day-of-month=\"1\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"month day\"> 1 </div>  </div>\n" +
        "</td>\n" +
        "<td id=\"events_calendar-2018-03-02-date-box\" class=\"date-box past\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-02\"  headers=\"Friday\"  data-day-of-month=\"2\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"month day\"> 2 </div>  </div>\n" +
        "</td>\n" +
        "<td id=\"events_calendar-2018-03-03-date-box\" class=\"date-box today\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-03\"  headers=\"Saturday\"  data-day-of-month=\"3\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"month day\"> 3 </div>  </div>\n" +
        "</td>\n" + "</tr>\n" + "<tr class=\"single-day\">\n" +
        "  <td id=\"events_calendar-2018-02-25-0\" class=\"single-day past empty\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-02-25\"  headers=\"Sunday\"  data-day-of-month=\"25\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"calendar-empty\">&nbsp;</div>\n" +
        "  </div>\n" + "</td>\n" +
        "<td id=\"events_calendar-2018-02-26-0\" class=\"single-day past empty\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-02-26\"  headers=\"Monday\"  data-day-of-month=\"26\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"calendar-empty\">&nbsp;</div>\n" +
        "  </div>\n" + "</td>\n" +
        "<td id=\"events_calendar-2018-02-27-0\" class=\"single-day past empty\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-02-27\"  headers=\"Tuesday\"  data-day-of-month=\"27\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"calendar-empty\">&nbsp;</div>\n" +
        "  </div>\n" + "</td>\n" +
        "<td id=\"events_calendar-2018-02-28-0\" class=\"single-day past empty\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-02-28\"  headers=\"Wednesday\"  data-day-of-month=\"28\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"calendar-empty\">&nbsp;</div>\n" +
        "  </div>\n" + "</td>\n" +
        "<td id=\"events_calendar-2018-03-01-0\" class=\"single-day no-entry past\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-01\"  headers=\"Thursday\"  data-day-of-month=\"1\" >\n" +
        "  <div class=\"inner\">\n" + "    &nbsp;  </div>\n" + "</td>\n" +
        "<td id=\"events_calendar-2018-03-02-0\" class=\"single-day past\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-02\"  headers=\"Friday\"  data-day-of-month=\"2\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"item\">\n" +
        "  <div class=\"view-item view-item-events_calendar\">\n" +
        "  <div class=\"calendar monthview\">\n" +
        "        <div class=\"calendar.419.field_event_date.0.0 contents\">\n" +
        "                        \n" +
        "  <div class=\"views-field views-field-title\">        <span class=\"field-content\"><a href=\"/events/hailstorm-brewing-co\">Hailstorm brewing Co.</a></span>  </div>  \n" +
        "  <div class=\"views-field views-field-field-event-date\">        <div class=\"field-content\"><span class=\"date-display-single\">Mar 2 2018 - <div class=\"date-display-range\"><span class=\"date-display-start\">5:00pm</span> to <span class=\"date-display-end\">9:00pm</span></div></span></div>  </div>          </div>  \n" +
        "        <div class=\"cutoff\">&nbsp;</div>\n" + "      </div> \n" + "  </div>   \n" +
        "</div>  </div>\n" + "</td>\n" +
        "<td id=\"events_calendar-2018-03-03-0\" class=\"single-day today\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-03\"  headers=\"Saturday\"  data-day-of-month=\"3\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"item\">\n" +
        "  <div class=\"view-item view-item-events_calendar\">\n" +
        "  <div class=\"calendar monthview\">\n" +
        "        <div class=\"calendar.393.field_event_date.0.0 contents\">\n" +
        "                        \n" +
        "  <div class=\"views-field views-field-title\">        <span class=\"field-content\"><a href=\"/events/pollyanna-brewing-lemont\">Pollyanna Brewing--Lemont</a></span>  </div>  \n" +
        "  <div class=\"views-field views-field-field-event-date\">        <div class=\"field-content\"><span class=\"date-display-single\">Mar 3 2018 - <div class=\"date-display-range\"><span class=\"date-display-start\">5:00pm</span> to <span class=\"date-display-end\">9:00pm</span></div></span></div>  </div>          </div>  \n" +
        "        <div class=\"cutoff\">&nbsp;</div>\n" + "      </div> \n" + "  </div>   \n" +
        "</div>  </div>\n" + "</td>\n" + "</tr>\n" + "<tr class=\"date-box\">\n" +
        "  <td id=\"events_calendar-2018-03-04-date-box\" class=\"date-box future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-04\"  headers=\"Sunday\"  data-day-of-month=\"4\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"month day\"> 4 </div>  </div>\n" +
        "</td>\n" +
        "<td id=\"events_calendar-2018-03-05-date-box\" class=\"date-box future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-05\"  headers=\"Monday\"  data-day-of-month=\"5\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"month day\"> 5 </div>  </div>\n" +
        "</td>\n" +
        "<td id=\"events_calendar-2018-03-06-date-box\" class=\"date-box future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-06\"  headers=\"Tuesday\"  data-day-of-month=\"6\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"month day\"> 6 </div>  </div>\n" +
        "</td>\n" +
        "<td id=\"events_calendar-2018-03-07-date-box\" class=\"date-box future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-07\"  headers=\"Wednesday\"  data-day-of-month=\"7\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"month day\"> 7 </div>  </div>\n" +
        "</td>\n" +
        "<td id=\"events_calendar-2018-03-08-date-box\" class=\"date-box future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-08\"  headers=\"Thursday\"  data-day-of-month=\"8\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"month day\"> 8 </div>  </div>\n" +
        "</td>\n" +
        "<td id=\"events_calendar-2018-03-09-date-box\" class=\"date-box future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-09\"  headers=\"Friday\"  data-day-of-month=\"9\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"month day\"> 9 </div>  </div>\n" +
        "</td>\n" +
        "<td id=\"events_calendar-2018-03-10-date-box\" class=\"date-box future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-10\"  headers=\"Saturday\"  data-day-of-month=\"10\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"month day\"> 10 </div>  </div>\n" +
        "</td>\n" + "</tr>\n" + "<tr class=\"single-day\">\n" +
        "  <td id=\"events_calendar-2018-03-04-0\" class=\"single-day no-entry future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-04\"  headers=\"Sunday\"  data-day-of-month=\"4\" >\n" +
        "  <div class=\"inner\">\n" + "    &nbsp;  </div>\n" + "</td>\n" +
        "<td id=\"events_calendar-2018-03-05-0\" class=\"single-day no-entry future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-05\"  headers=\"Monday\"  data-day-of-month=\"5\" >\n" +
        "  <div class=\"inner\">\n" + "    &nbsp;  </div>\n" + "</td>\n" +
        "<td id=\"events_calendar-2018-03-06-0\" class=\"single-day no-entry future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-06\"  headers=\"Tuesday\"  data-day-of-month=\"6\" >\n" +
        "  <div class=\"inner\">\n" + "    &nbsp;  </div>\n" + "</td>\n" +
        "<td id=\"events_calendar-2018-03-07-0\" class=\"single-day no-entry future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-07\"  headers=\"Wednesday\"  data-day-of-month=\"7\" >\n" +
        "  <div class=\"inner\">\n" + "    &nbsp;  </div>\n" + "</td>\n" +
        "<td id=\"events_calendar-2018-03-08-0\" class=\"single-day no-entry future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-08\"  headers=\"Thursday\"  data-day-of-month=\"8\" >\n" +
        "  <div class=\"inner\">\n" + "    &nbsp;  </div>\n" + "</td>\n" +
        "<td id=\"events_calendar-2018-03-09-0\" class=\"single-day future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-09\"  headers=\"Friday\"  data-day-of-month=\"9\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"item\">\n" +
        "  <div class=\"view-item view-item-events_calendar\">\n" +
        "  <div class=\"calendar monthview\">\n" +
        "        <div class=\"calendar.415.field_event_date.0.0 contents\">\n" +
        "                        \n" +
        "  <div class=\"views-field views-field-title\">        <span class=\"field-content\"><a href=\"/events/imperial-oaks-brewing-26\">Imperial Oaks brewing</a></span>  </div>  \n" +
        "  <div class=\"views-field views-field-field-event-date\">        <div class=\"field-content\"><span class=\"date-display-single\">Mar 9 2018 - <div class=\"date-display-range\"><span class=\"date-display-start\">5:00pm</span> to <span class=\"date-display-end\">9:00pm</span></div></span></div>  </div>          </div>  \n" +
        "        <div class=\"cutoff\">&nbsp;</div>\n" + "      </div> \n" + "  </div>   \n" +
        "</div>  </div>\n" + "</td>\n" +
        "<td id=\"events_calendar-2018-03-10-0\" class=\"single-day future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-10\"  headers=\"Saturday\"  data-day-of-month=\"10\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"item\">\n" +
        "  <div class=\"view-item view-item-events_calendar\">\n" +
        "  <div class=\"calendar monthview\">\n" +
        "        <div class=\"calendar.416.field_event_date.0.0 contents\">\n" +
        "                        \n" +
        "  <div class=\"views-field views-field-title\">        <span class=\"field-content\"><a href=\"/events/imperial-oaks-brewing-27\">Imperial Oaks Brewing</a></span>  </div>  \n" +
        "  <div class=\"views-field views-field-field-event-date\">        <div class=\"field-content\"><span class=\"date-display-single\">Mar 10 2018 - <div class=\"date-display-range\"><span class=\"date-display-start\">11:00am</span> to <span class=\"date-display-end\">4:00pm</span></div></span></div>  </div>          </div>  \n" +
        "        <div class=\"cutoff\">&nbsp;</div>\n" + "      </div> \n" + "  </div>   \n" +
        "</div><div class=\"item\">\n" + "  <div class=\"view-item view-item-events_calendar\">\n" +
        "  <div class=\"calendar monthview\">\n" +
        "        <div class=\"calendar.392.field_event_date.0.0 contents\">\n" +
        "                        \n" +
        "  <div class=\"views-field views-field-title\">        <span class=\"field-content\"><a href=\"/events/pollyanna-brewing-roselle-0\">Pollyanna Brewing---Roselle</a></span>  </div>  \n" +
        "  <div class=\"views-field views-field-field-event-date\">        <div class=\"field-content\"><span class=\"date-display-single\">Mar 10 2018 - <div class=\"date-display-range\"><span class=\"date-display-start\">5:00pm</span> to <span class=\"date-display-end\">9:00pm</span></div></span></div>  </div>          </div>  \n" +
        "        <div class=\"cutoff\">&nbsp;</div>\n" + "      </div> \n" + "  </div>   \n" +
        "</div>  </div>\n" + "</td>\n" + "</tr>\n" + "<tr class=\"date-box\">\n" +
        "  <td id=\"events_calendar-2018-03-11-date-box\" class=\"date-box future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-11\"  headers=\"Sunday\"  data-day-of-month=\"11\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"month day\"> 11 </div>  </div>\n" +
        "</td>\n" +
        "<td id=\"events_calendar-2018-03-12-date-box\" class=\"date-box future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-12\"  headers=\"Monday\"  data-day-of-month=\"12\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"month day\"> 12 </div>  </div>\n" +
        "</td>\n" +
        "<td id=\"events_calendar-2018-03-13-date-box\" class=\"date-box future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-13\"  headers=\"Tuesday\"  data-day-of-month=\"13\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"month day\"> 13 </div>  </div>\n" +
        "</td>\n" +
        "<td id=\"events_calendar-2018-03-14-date-box\" class=\"date-box future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-14\"  headers=\"Wednesday\"  data-day-of-month=\"14\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"month day\"> 14 </div>  </div>\n" +
        "</td>\n" +
        "<td id=\"events_calendar-2018-03-15-date-box\" class=\"date-box future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-15\"  headers=\"Thursday\"  data-day-of-month=\"15\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"month day\"> 15 </div>  </div>\n" +
        "</td>\n" +
        "<td id=\"events_calendar-2018-03-16-date-box\" class=\"date-box future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-16\"  headers=\"Friday\"  data-day-of-month=\"16\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"month day\"> 16 </div>  </div>\n" +
        "</td>\n" +
        "<td id=\"events_calendar-2018-03-17-date-box\" class=\"date-box future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-17\"  headers=\"Saturday\"  data-day-of-month=\"17\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"month day\"> 17 </div>  </div>\n" +
        "</td>\n" + "</tr>\n" + "<tr class=\"single-day\">\n" +
        "  <td id=\"events_calendar-2018-03-11-0\" class=\"single-day no-entry future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-11\"  headers=\"Sunday\"  data-day-of-month=\"11\" >\n" +
        "  <div class=\"inner\">\n" + "    &nbsp;  </div>\n" + "</td>\n" +
        "<td id=\"events_calendar-2018-03-12-0\" class=\"single-day no-entry future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-12\"  headers=\"Monday\"  data-day-of-month=\"12\" >\n" +
        "  <div class=\"inner\">\n" + "    &nbsp;  </div>\n" + "</td>\n" +
        "<td id=\"events_calendar-2018-03-13-0\" class=\"single-day no-entry future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-13\"  headers=\"Tuesday\"  data-day-of-month=\"13\" >\n" +
        "  <div class=\"inner\">\n" + "    &nbsp;  </div>\n" + "</td>\n" +
        "<td id=\"events_calendar-2018-03-14-0\" class=\"single-day no-entry future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-14\"  headers=\"Wednesday\"  data-day-of-month=\"14\" >\n" +
        "  <div class=\"inner\">\n" + "    &nbsp;  </div>\n" + "</td>\n" +
        "<td id=\"events_calendar-2018-03-15-0\" class=\"single-day no-entry future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-15\"  headers=\"Thursday\"  data-day-of-month=\"15\" >\n" +
        "  <div class=\"inner\">\n" + "    &nbsp;  </div>\n" + "</td>\n" +
        "<td id=\"events_calendar-2018-03-16-0\" class=\"single-day future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-16\"  headers=\"Friday\"  data-day-of-month=\"16\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"item\">\n" +
        "  <div class=\"view-item view-item-events_calendar\">\n" +
        "  <div class=\"calendar monthview\">\n" +
        "        <div class=\"calendar.420.field_event_date.0.0 contents\">\n" +
        "                        \n" +
        "  <div class=\"views-field views-field-title\">        <span class=\"field-content\"><a href=\"/events/hailstorm-brewing-co-0\">Hailstorm Brewing Co.</a></span>  </div>  \n" +
        "  <div class=\"views-field views-field-field-event-date\">        <div class=\"field-content\"><span class=\"date-display-single\">Mar 16 2018 - <div class=\"date-display-range\"><span class=\"date-display-start\">5:00pm</span> to <span class=\"date-display-end\">9:00pm</span></div></span></div>  </div>          </div>  \n" +
        "        <div class=\"cutoff\">&nbsp;</div>\n" + "      </div> \n" + "  </div>   \n" +
        "</div>  </div>\n" + "</td>\n" +
        "<td id=\"events_calendar-2018-03-17-0\" class=\"single-day future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-17\"  headers=\"Saturday\"  data-day-of-month=\"17\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"item\">\n" +
        "  <div class=\"view-item view-item-events_calendar\">\n" +
        "  <div class=\"calendar monthview\">\n" +
        "        <div class=\"calendar.402.field_event_date.0.0 contents\">\n" +
        "                        \n" +
        "  <div class=\"views-field views-field-title\">        <span class=\"field-content\"><a href=\"/events/tailwinds-distillery-and-workforce-pub-crawl\">Tailwinds Distillery and WorkForce --Pub Crawl</a></span>  </div>  \n" +
        "  <div class=\"views-field views-field-field-event-date\">        <div class=\"field-content\"><span class=\"date-display-single\">Mar 17 2018 - <div class=\"date-display-range\"><span class=\"date-display-start\">1:00pm</span> to <span class=\"date-display-end\">5:00pm</span></div></span></div>  </div>          </div>  \n" +
        "        <div class=\"cutoff\">&nbsp;</div>\n" + "      </div> \n" + "  </div>   \n" +
        "</div>  </div>\n" + "</td>\n" + "</tr>\n" + "<tr class=\"date-box\">\n" +
        "  <td id=\"events_calendar-2018-03-18-date-box\" class=\"date-box future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-18\"  headers=\"Sunday\"  data-day-of-month=\"18\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"month day\"> 18 </div>  </div>\n" +
        "</td>\n" +
        "<td id=\"events_calendar-2018-03-19-date-box\" class=\"date-box future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-19\"  headers=\"Monday\"  data-day-of-month=\"19\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"month day\"> 19 </div>  </div>\n" +
        "</td>\n" +
        "<td id=\"events_calendar-2018-03-20-date-box\" class=\"date-box future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-20\"  headers=\"Tuesday\"  data-day-of-month=\"20\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"month day\"> 20 </div>  </div>\n" +
        "</td>\n" +
        "<td id=\"events_calendar-2018-03-21-date-box\" class=\"date-box future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-21\"  headers=\"Wednesday\"  data-day-of-month=\"21\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"month day\"> 21 </div>  </div>\n" +
        "</td>\n" +
        "<td id=\"events_calendar-2018-03-22-date-box\" class=\"date-box future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-22\"  headers=\"Thursday\"  data-day-of-month=\"22\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"month day\"> 22 </div>  </div>\n" +
        "</td>\n" +
        "<td id=\"events_calendar-2018-03-23-date-box\" class=\"date-box future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-23\"  headers=\"Friday\"  data-day-of-month=\"23\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"month day\"> 23 </div>  </div>\n" +
        "</td>\n" +
        "<td id=\"events_calendar-2018-03-24-date-box\" class=\"date-box future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-24\"  headers=\"Saturday\"  data-day-of-month=\"24\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"month day\"> 24 </div>  </div>\n" +
        "</td>\n" + "</tr>\n" + "<tr class=\"multi-day\">\n" +
        "  <td id=\"events_calendar-2018-03-18-0\" class=\"single-day future\" colspan=\"1\" rowspan=\"2\" data-date=\"2018-03-18\"  headers=\"Sunday\"  data-day-of-month=\"18\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"item\">\n" +
        "  <div class=\"view-item view-item-events_calendar\">\n" +
        "  <div class=\"calendar monthview\">\n" +
        "        <div class=\"calendar.417.field_event_date.0.0 contents\">\n" +
        "                        \n" +
        "  <div class=\"views-field views-field-title\">        <span class=\"field-content\"><a href=\"/events/imperial-oaks-brewing-28\">Imperial oaks Brewing</a></span>  </div>  \n" +
        "  <div class=\"views-field views-field-field-event-date\">        <div class=\"field-content\"><span class=\"date-display-single\">Mar 18 2018 - <div class=\"date-display-range\"><span class=\"date-display-start\">1:00pm</span> to <span class=\"date-display-end\">5:00pm</span></div></span></div>  </div>          </div>  \n" +
        "        <div class=\"cutoff\">&nbsp;</div>\n" + "      </div> \n" + "  </div>   \n" +
        "</div>  </div>\n" + "</td>\n" +
        "<td id=\"events_calendar-2018-03-19-0\" class=\"single-day future\" colspan=\"1\" rowspan=\"2\" data-date=\"2018-03-19\"  headers=\"Monday\"  data-day-of-month=\"19\" >\n" +
        "  <div class=\"inner\">\n" + "    &nbsp;  </div>\n" + "</td>\n" +
        "<td id=\"events_calendar-2018-03-20-0\" class=\"single-day future\" colspan=\"1\" rowspan=\"2\" data-date=\"2018-03-20\"  headers=\"Tuesday\"  data-day-of-month=\"20\" >\n" +
        "  <div class=\"inner\">\n" + "    &nbsp;  </div>\n" + "</td>\n" +
        "<td id=\"events_calendar-2018-03-21-0\" class=\"single-day future\" colspan=\"1\" rowspan=\"2\" data-date=\"2018-03-21\"  headers=\"Wednesday\"  data-day-of-month=\"21\" >\n" +
        "  <div class=\"inner\">\n" + "    &nbsp;  </div>\n" + "</td>\n" +
        "<td class=\"multi-day\" colspan=\"3\" rowspan=\"1\" data-date=\"2018-03-22\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"item\">\n" +
        "  <div class=\"view-item view-item-events_calendar\">\n" +
        "  <div class=\"calendar monthview\">\n" +
        "        <div class=\"calendar.418.field_event_date.0.0 contents\">\n" +
        "                      <a href=\"/events/southwest-bbq-trip\">SouthWest BBQ Trip</a>                      <div class=\"date-display-range\"><span class=\"date-display-start\">Mar 22 2018 - 12:00am</span> to <span class=\"date-display-end\">Mar 29 2018 - 12:00am</span></div>          </div>  \n" +
        "        <div class=\"continues\">&raquo;</div>\n" + "      </div> \n" + "  </div>   \n" +
        "</div>  </div>\n" + "</td>\n" + "</tr>\n" + "<tr class=\"single-day\" iehint=\"1\">\n" +
        "  <td id=\"events_calendar-2018-03-22-1\" class=\"single-day future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-22\"  headers=\"Thursday\"  data-day-of-month=\"22\" >\n" +
        "  <div class=\"inner\">\n" + "    &nbsp;  </div>\n" + "</td>\n" +
        "<td id=\"events_calendar-2018-03-23-1\" class=\"single-day future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-23\"  headers=\"Friday\"  data-day-of-month=\"23\" >\n" +
        "  <div class=\"inner\">\n" + "    &nbsp;  </div>\n" + "</td>\n" +
        "<td id=\"events_calendar-2018-03-24-1\" class=\"single-day future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-24\"  headers=\"Saturday\"  data-day-of-month=\"24\" >\n" +
        "  <div class=\"inner\">\n" + "    &nbsp;  </div>\n" + "</td>\n" + "</tr>\n" +
        "<tr class=\"date-box\">\n" +
        "  <td id=\"events_calendar-2018-03-25-date-box\" class=\"date-box future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-25\"  headers=\"Sunday\"  data-day-of-month=\"25\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"month day\"> 25 </div>  </div>\n" +
        "</td>\n" +
        "<td id=\"events_calendar-2018-03-26-date-box\" class=\"date-box future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-26\"  headers=\"Monday\"  data-day-of-month=\"26\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"month day\"> 26 </div>  </div>\n" +
        "</td>\n" +
        "<td id=\"events_calendar-2018-03-27-date-box\" class=\"date-box future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-27\"  headers=\"Tuesday\"  data-day-of-month=\"27\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"month day\"> 27 </div>  </div>\n" +
        "</td>\n" +
        "<td id=\"events_calendar-2018-03-28-date-box\" class=\"date-box future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-28\"  headers=\"Wednesday\"  data-day-of-month=\"28\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"month day\"> 28 </div>  </div>\n" +
        "</td>\n" +
        "<td id=\"events_calendar-2018-03-29-date-box\" class=\"date-box future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-29\"  headers=\"Thursday\"  data-day-of-month=\"29\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"month day\"> 29 </div>  </div>\n" +
        "</td>\n" +
        "<td id=\"events_calendar-2018-03-30-date-box\" class=\"date-box future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-30\"  headers=\"Friday\"  data-day-of-month=\"30\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"month day\"> 30 </div>  </div>\n" +
        "</td>\n" +
        "<td id=\"events_calendar-2018-03-31-date-box\" class=\"date-box future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-31\"  headers=\"Saturday\"  data-day-of-month=\"31\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"month day\"> 31 </div>  </div>\n" +
        "</td>\n" + "</tr>\n" + "<tr class=\"multi-day\">\n" +
        "  <td class=\"multi-day\" colspan=\"5\" rowspan=\"1\" data-date=\"2018-03-25\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"item\">\n" +
        "  <div class=\"view-item view-item-events_calendar\">\n" +
        "  <div class=\"calendar monthview\">\n" +
        "        <div class=\"calendar.418.field_event_date.0.3 contents\">\n" +
        "                      <div class=\"continuation\">&laquo;</div>\n" +
        "                <a href=\"/events/southwest-bbq-trip\">SouthWest BBQ Trip</a>                      <div class=\"date-display-range\"><span class=\"date-display-start\">Mar 22 2018 - 12:00am</span> to <span class=\"date-display-end\">Mar 29 2018 - 12:00am</span></div>          </div>  \n" +
        "        <div class=\"cutoff\">&nbsp;</div>\n" + "      </div> \n" + "  </div>   \n" +
        "</div>  </div>\n" + "</td>\n" +
        "<td id=\"events_calendar-2018-03-30-0\" class=\"single-day future\" colspan=\"1\" rowspan=\"2\" data-date=\"2018-03-30\"  headers=\"Friday\"  data-day-of-month=\"30\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"item\">\n" +
        "  <div class=\"view-item view-item-events_calendar\">\n" +
        "  <div class=\"calendar monthview\">\n" +
        "        <div class=\"calendar.421.field_event_date.0.0 contents\">\n" +
        "                        \n" +
        "  <div class=\"views-field views-field-title\">        <span class=\"field-content\"><a href=\"/events/hailstorm-brewing-co-1\">Hailstorm Brewing Co.</a></span>  </div>  \n" +
        "  <div class=\"views-field views-field-field-event-date\">        <div class=\"field-content\"><span class=\"date-display-single\">Mar 30 2018 - <div class=\"date-display-range\"><span class=\"date-display-start\">5:00pm</span> to <span class=\"date-display-end\">9:00pm</span></div></span></div>  </div>          </div>  \n" +
        "        <div class=\"cutoff\">&nbsp;</div>\n" + "      </div> \n" + "  </div>   \n" +
        "</div>  </div>\n" + "</td>\n" +
        "<td id=\"events_calendar-2018-03-31-0\" class=\"single-day future\" colspan=\"1\" rowspan=\"2\" data-date=\"2018-03-31\"  headers=\"Saturday\"  data-day-of-month=\"31\" >\n" +
        "  <div class=\"inner\">\n" + "    <div class=\"item\">\n" +
        "  <div class=\"view-item view-item-events_calendar\">\n" +
        "  <div class=\"calendar monthview\">\n" +
        "        <div class=\"calendar.408.field_event_date.0.0 contents\">\n" +
        "                        \n" +
        "  <div class=\"views-field views-field-title\">        <span class=\"field-content\"><a href=\"/events/pollyanna-brewing-roselle-2\">Pollyanna Brewing---Roselle</a></span>  </div>  \n" +
        "  <div class=\"views-field views-field-field-event-date\">        <div class=\"field-content\"><span class=\"date-display-single\">Mar 31 2018 - <div class=\"date-display-range\"><span class=\"date-display-start\">1:00pm</span> to <span class=\"date-display-end\">6:00pm</span></div></span></div>  </div>          </div>  \n" +
        "        <div class=\"cutoff\">&nbsp;</div>\n" + "      </div> \n" + "  </div>   \n" +
        "</div>  </div>\n" + "</td>\n" + "</tr>\n" + "<tr class=\"single-day\" iehint=\"1\">\n" +
        "  <td id=\"events_calendar-2018-03-25-1\" class=\"single-day future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-25\"  headers=\"Sunday\"  data-day-of-month=\"25\" >\n" +
        "  <div class=\"inner\">\n" + "    &nbsp;  </div>\n" + "</td>\n" +
        "<td id=\"events_calendar-2018-03-26-1\" class=\"single-day future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-26\"  headers=\"Monday\"  data-day-of-month=\"26\" >\n" +
        "  <div class=\"inner\">\n" + "    &nbsp;  </div>\n" + "</td>\n" +
        "<td id=\"events_calendar-2018-03-27-1\" class=\"single-day future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-27\"  headers=\"Tuesday\"  data-day-of-month=\"27\" >\n" +
        "  <div class=\"inner\">\n" + "    &nbsp;  </div>\n" + "</td>\n" +
        "<td id=\"events_calendar-2018-03-28-1\" class=\"single-day future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-28\"  headers=\"Wednesday\"  data-day-of-month=\"28\" >\n" +
        "  <div class=\"inner\">\n" + "    &nbsp;  </div>\n" + "</td>\n" +
        "<td id=\"events_calendar-2018-03-29-1\" class=\"single-day future\" colspan=\"1\" rowspan=\"1\" data-date=\"2018-03-29\"  headers=\"Thursday\"  data-day-of-month=\"29\" >\n" +
        "  <div class=\"inner\">\n" + "    &nbsp;  </div>\n" + "</td>\n" + "</tr>\n" +
        "  </tbody>\n" + "</table>\n" + "</div></div>\n" + "<script>\n" + "try {\n" +
        "  // ie hack to make the single day row expand to available space\n" +
        "  if ($.browser.msie ) {\n" +
        "    var multiday_height = $('tr.multi-day')[0].clientHeight; // Height of a multi-day row\n" +
        "    $('tr[iehint]').each(function(index) {\n" +
        "      var iehint = this.getAttribute('iehint');\n" +
        "      // Add height of the multi day rows to the single day row - seems that 80% height works best\n" +
        "      var height = this.clientHeight + (multiday_height * .8 * iehint); \n" +
        "      this.style.height = height + 'px';\n" + "    });\n" + "  }\n" + "}catch(e){\n" +
        "  // swallow \n" + "}\n" + "</script>    </div>\n" + "  \n" + "  \n" + "  \n" +
        "  </div>\n" + "  </div>\n" + "\n" + "  \n" + "  </div>\n" + "    </div>\n" + "  \n" +
        "  </div>\n" + "      </main>\n" + "\n" + "  <footer class=\"footer\" role=\"footer\">\n" +
        "          <div class=\"container footer-text\">\n" +
        "        <p>Chicagoland's Best new BBQ Food Truck!</p>      </div>\n" +
        "        <div class=\"container\">\n" + "              <ul class=\"nav footer-nav\">\n" +
        "          <li class=\"first leaf active menu-link-schedule\"><a href=\"/events/calendar\" title=\"\" class=\"active\">Schedule</a></li>\n" +
        "<li class=\"leaf menu-link-menu\"><a href=\"/menus\" title=\"\">Menu</a></li>\n" +
        "<li class=\"last leaf menu-link-about-us\"><a href=\"/about\" title=\"\">About Us</a></li>\n" +
        "        </ul>\n" + "      \n" + "              <div class=\"copyright\">\n" +
        "          <div class=\"text--center\"><h4>Follow us on</h4><a href=\"\"><i class=\"fa fa-yelp\"> </i></a> <a href=\"https://twitter.com/SmokinBBQKitchn\"><i class=\"fa fa-twitter-square\"> </i></a> <a href=\"https://www.facebook.com/SmokinBBQKitchen\"><i class=\"fa fa-facebook-square\"> </i></a> <a href=\"\"><i class=\"fa fa-google-plus-square\"> </i></a><p class=\"margin--sm--top\">@ 2016 Smokin' BBQ Kitchen</p></div>        </div>\n" +
        "          </div>\n" + "  </footer>\n" +
        "</div><script type=\"text/javascript\" src=\"http://smokinbbqkitchen.com/profiles/restaurant/modules/contrib/panopoly_widgets/panopoly-widgets.js?obaqd3\"></script>\n" +
        "<script type=\"text/javascript\" src=\"http://smokinbbqkitchen.com/profiles/restaurant/modules/contrib/panopoly_widgets/panopoly-widgets-spotlight.js?obaqd3\"></script>\n" +
        "<div class=\"device-xs visible-xs\"></div>\n" +
        "<div class=\"device-sm visible-sm\"></div>\n" +
        "<div class=\"device-md visible-md\"></div>\n" +
        "<div class=\"device-lg visible-lg\"></div>\n" + "</body>\n" + "</html>";
    Truck truck = Truck.builder().id("smokingbbqkitchen").name("BBQ Truck").build();
    when(geoLocator.locate("Imperial Oaks Brewing", GeolocationGranularity.NARROW))
        .thenReturn(wackerAndAdams());
    when(geoLocator.locate("Pollyanna Brewing---Roselle", GeolocationGranularity.NARROW))
        .thenReturn(clarkAndMonroe());
    DrupalCalendarStopReader provider = new DrupalCalendarStopReader(geoLocator, DateTimeZone.UTC);
    List<TruckStop> result = provider.read(input, truck);
    assertThat(result).containsExactly(TruckStop.builder()
        .startTime(new DateTime(2018, 3, 10, 11, 0, DateTimeZone.UTC))
        .endTime(new DateTime(2018, 3, 10, 16, 0, DateTimeZone.UTC))
        .location(wackerAndAdams())
        .truck(truck)
        .build(), TruckStop.builder()
        .startTime(new DateTime(2018, 3, 10, 17, 0, DateTimeZone.UTC))
        .endTime(new DateTime(2018, 3, 10, 21, 0, DateTimeZone.UTC))
        .location(clarkAndMonroe())
        .truck(truck)
        .build(),TruckStop.builder()
        .startTime(new DateTime(2018, 3, 31, 13, 0, DateTimeZone.UTC))
        .endTime(new DateTime(2018, 3, 31, 18, 0, DateTimeZone.UTC))
        .location(clarkAndMonroe())
        .truck(truck)
        .build());

  }
}