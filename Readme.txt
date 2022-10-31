1. git clone
2. 调整idea内存，至少4G:Help-change memory setting
3. Java环境至少17
4. 编译
```bash
 ./gradlew build --parallel -x test -x javadoc -x check --build-cache --configure-on-demand
```
5. 证书和配置(/etc/cas/)准备
```properties
cas.server.name=https://cas.sayi.com:8443
cas.server.prefix=${cas.server.name}/cas
# cas.server.tomcat.http.enabled=true
server.ssl.enabled=true
server.port=8443
server.servlet.context-path=/cas

cas.tgc.secure=false
logging.config=file:/etc/cas/config/log4j2.xml


# cas.authn.accept.users=

## SAML2 IdP
cas.authn.samlIdp.entityId=${cas.server.prefix}/idp
# cas.authn.samlIdp.scope=sayi.com
cas.authn.samlIdp.metadata.location=file:/etc/cas/saml

cas.serviceRegistry.watcherEnabled=true
cas.serviceRegistry.initFromJson=true
cas.serviceRegistry.managementType=DEFAULT
cas.serviceRegistry.json.location=file:/etc/cas/services

# cas.authn.oauth.replicateSessions=false
cas.authn.oauth.grants.resourceOwner.requireServiceHeader=true

cas.authn.oauth.userProfileViewType=NESTED

cas.authn.oauth.refreshToken.timeToKillInSeconds=2592000
cas.authn.oauth.code.timeToKillInSeconds=300
cas.authn.oauth.code.numberOfUses=1
cas.authn.oauth.accessToken.timeToKillInSeconds=7200
cas.authn.oauth.accessToken.maxTimeToLiveInSeconds=28800
```
6. deploy
```
cd webapp/cas-server-webapp-tomcat
../../gradlew build bootRun --parallel -x test -x check --offline --configure-on-demand --build-cache --stacktrace
```


## puppeteer test
```
yarn add puppeteer

cd develop/GitHub/cas-server/ci/tests/puppeteer
yarn install

./gradlew --build-cache --configure-on-demand --no-daemon -q puppeteerScenarios
./ci/tests/puppeteer/run.sh --scenario ./ci/tests/puppeteer/scenarios/gua-login
```