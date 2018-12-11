const puppeteer = require('puppeteer');
let page;

async function getBrowserPage() {
  // Launch headless Chrome. Turn off sandbox so Chrome can run under root.
  const browser = await puppeteer.launch({args: ['--no-sandbox']});
  return browser.newPage();
}

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


  await page.goto('http://www.imperialoakbrewing.com/category/food-trucks/', {waitUntil: 'network\
idle2', timeout: 60000});
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
  res.status(200).send(JSON.stringify(items))
};

