const puppeteer = require('puppeteer');
const path = require('path');
const cas = require('../../cas.js');
const assert = require("assert");

async function cleanUp() {
    await cas.removeDirectory(path.join(__dirname, '/saml-md'));
}

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    const response = await cas.goto(page, "https://localhost:8443/cas/idp/metadata");
    console.log(`${response.status()} ${response.statusText()}`);
    assert(response.ok());
    
    await cas.waitFor('https://localhost:9876/sp/saml/status', async () => {
        console.log("Trying without an exising SSO session...");
        await cas.goto(page, "https://localhost:9876/sp");
        await page.waitForTimeout(3000);
        await page.waitForSelector('#idpForm', {visible: true});
        await cas.submitForm(page, "#idpForm");
        await page.waitForTimeout(3000);
        await cas.assertInnerText(page, "#content h2", "Application Not Authorized to Use CAS");

        console.log("Trying with an exising SSO session...");
        await cas.goto(page, "https://localhost:8443/cas/logout");
        await cas.goto(page, "https://localhost:8443/cas/login");
        await cas.loginWith(page, "casuser", "Mellon");
        await cas.assertCookie(page);
        await cas.goto(page, "https://localhost:9876/sp");
        await page.waitForTimeout(2000);
        await page.waitForSelector('#idpForm', {visible: true});
        await cas.submitForm(page, "#idpForm");
        await page.waitForTimeout(2000);
        await cas.assertInnerText(page, "#content h2", "Application Not Authorized to Use CAS");

        await browser.close();
        await cleanUp();
    }, async error => {
        await cleanUp();
        console.log(error);
        throw error;
    })
})();

