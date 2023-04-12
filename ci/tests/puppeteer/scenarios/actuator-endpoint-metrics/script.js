const puppeteer = require('puppeteer');
const cas = require('../../cas.js');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());

    for (let i = 0; i < 5; i++) {
        const page = await cas.newPage(browser);
        await cas.goto(page, "https://localhost:8443/cas/login");
        await cas.loginWith(page, "casuser", "Mellon");
        await cas.assertCookie(page);
        await cas.goto(page, "https://localhost:8443/cas/logout");
        await cas.assertCookie(page, false);
        await cas.goto(page, "https://localhost:8443/cas/actuator/registeredServices");
        await page.close();
    }
    await browser.close();
    const baseUrl = "https://localhost:8443/cas/actuator";
    await cas.doRequest(`${baseUrl}/metrics`, "GET", {
        'Accept': 'application/json', 'Content-Type': 'application/json'
    }, 200);
    await cas.doRequest(`${baseUrl}/prometheus`, "GET", {}, 200);
})();

