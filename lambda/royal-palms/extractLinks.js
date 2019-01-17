const puppeteer = require('puppeteer');
(async() => {
  const browser = await puppeteer.launch();
  const page = await browser.newPage();
  await page.goto('https://www.royalpalmschicago.com/calendar/', {waitUntil: 'networkidle2'});
  await page.waitFor(2000);
  const hrefs  = await page.evaluate(() => {
      const list = document.querySelectorAll("a");
      const hrefs = [];
      const hrefHash = {};
      for (const element of list) {

        if (/events-calendar/.exec(element.href)) {
          if (!hrefHash[element.href]) {
            hrefs.push(element.href + "?format=ical");
            hrefHash[element.href] = 1;
          }
        }
      }
      return hrefs;
    });

  console.log(JSON.stringify(hrefs));

  await browser.close();
})();