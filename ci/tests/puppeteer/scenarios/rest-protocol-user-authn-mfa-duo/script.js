const assert = require('assert');
const cas = require('../../cas.js');

(async () => {
    const codes = await cas.fetchDuoSecurityBypassCodes("casuser");
    const url = `https://localhost:8443/cas/v1/users`;
    const body = await cas.doRequest(`${url}?username=casuser&password=Mellon&passcode=${codes[0]}`, "POST",
        {
            'Accept': 'application/json',
            'Content-Type': 'application/x-www-form-urlencoded'
        }, 200);
    let result = JSON.parse(body);
    console.dir(result, {depth: null, colors: true});
    assert(result.authentication.authenticationDate !== undefined);
    assert(result.authentication.principal.id === "casuser");
    assert(result.authentication.attributes.authnContextClass[0] === "mfa-duo");

    const staticAuthN = result.authentication.successes["STATIC"];
    assert(staticAuthN.principal.id === "casuser");
    assert(staticAuthN.credential.credentialMetadata.credentialClass.includes("UsernamePasswordCredential"));
    assert(staticAuthN.credential.credentialMetadata.id === "casuser");

    const handler = result.authentication.successes["DuoSecurityAuthenticationHandler"];
    assert(handler.credential.credentialMetadata.credentialClass.includes("DuoSecurityPasscodeCredential"));
    assert(handler.credential.credentialMetadata.id === "casuser");
    assert(handler.principal.id === "casuser");
})();
