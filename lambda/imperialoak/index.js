const puppeteer = require('puppeteer');
let page;
let browser;


async function extractData() {

  if (!page) {
    browser = await puppeteer.launch({args: ['--no-sandbox']});
    page = await browser.newPage();
  }

  await page.goto('http://www.imperialoakbrewing.com/category/food-trucks/', {waitUntil: 'network\
idle2', timeout: 60000});
  console.log("Foooz");
  const items  = await page.evaluate(() => {
      const list = document.querySelectorAll("td.simcal-day-has-events");
      const items = [];
      for (const element of list) {
        const events = element.querySelectorAll(".simcal-event");
        for (const evt of events) {
          items.push({
              "title": evt.querySelector(".simcal-event-title").innerText,
              "start": evt.querySelector(".simcal-event-start-time").getAttribute("content"),
              "end": evt.querySelector(".simcal-event-end-time").getAttribute("content")
                });


        }

      }
      return items;
    });
  await browser.close();
  return items;
}

(async() => {
  console.log(await extractData());

})();

/**
 * Responds to any HTTP request.
 *
 * @param {!express:Request} req HTTP request context.
 * @param {!express:Response} res HTTP response context.
 */
exports.schedule = async (req, res) => {

  if (!page) {
    page = await getBrowserPage();
  }

  await page.goto('http://www.imperialoakbrewing.com/category/food-trucks/', {waitUntil: 'networkidle2'});
  const hrefs  = await page.evaluate(() => {
      const list = document.querySelectorAll("td.simcal-day-has-events");
      const hrefs = [];
      const hrefHash = {};
      for (const element of list) {
        if (/new-events\/2/.exec(element.href)) {
          if (!hrefHash[element.href]) {
            hrefs.push(element.href + "?format=ical");
            hrefHash[element.href] = 1;
          }
        }
      }
      return hrefs;
  });
  res.status(200).send(hrefs)
};
