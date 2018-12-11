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

  await page.goto('http://chicagopizzaboss.com/new-events/', {waitUntil: 'networkidle2'});
  const hrefs  = await page.evaluate(() => {
      const list = document.querySelectorAll("a");
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
