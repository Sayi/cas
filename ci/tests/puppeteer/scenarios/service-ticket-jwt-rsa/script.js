const puppeteer = require('puppeteer');
const cas = require('../../cas.js');
const assert = require('assert');

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);
    const service = "https://httpbin.org/anything/1";
    await cas.goto(page, `https://localhost:8443/cas/login?service=${service}`);
    await cas.loginWith(page, "casuser", "Mellon");
    await page.waitForTimeout(2000);
    let ticket = await cas.assertTicketParameter(page);

    await cas.doGet(`https://localhost:8443/cas/actuator/jwtTicketSigningPublicKey?service=${service}`,
        res => {
            let publickey = res.data;

            // const keyPath = path.join(__dirname, 'public.key');
            // let publickey = fs.readFileSync(keyPath);

            cas.verifyJwt(ticket, publickey, {
                algorithms: ["RS512"],
                complete: true
            }).then(decoded => {
                let payload = decoded.payload;
                
                assert(payload.successfulAuthenticationHandlers === "Static Credentials");
                assert(payload.authenticationMethod === "Static Credentials");
                assert(payload.aud === "https://httpbin.org/anything/1");
                assert(payload.credentialType === "UsernamePasswordCredential");
                assert(payload.sub === "casuser");
                assert(payload.username === "casuser");
                assert(payload.email === "casuser@apereo.org");
                assert(payload.name === "CAS");
                assert(payload.gender === "female");
                assert(payload.jti.startsWith("ST-"));
            });
        }, error => {
            throw `Introspection operation failed: ${error}`;
        }, {
            'Content-Type': 'application/json'
        });
    
    await browser.close();
})();
