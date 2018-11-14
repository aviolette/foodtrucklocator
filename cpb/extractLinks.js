const puppeteer = require('puppeteer');

(async() => {
  const browser = await puppeteer.launch();
  const page = await browser.newPage();
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
  console.log(hrefs);

  await browser.close();
})();