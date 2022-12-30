---
layout: default
title: CAS - Release Notes
category: Planning
---

# 7.0.0-RC2 Release Notes

We strongly recommend that you take advantage of the release candidates as they come out. Waiting for a `GA` release is only going to set
you up for unpleasant surprises. A `GA` is [a tag and nothing more](https://apereo.github.io/2017/03/08/the-myth-of-ga-rel/). Note
that CAS releases are *strictly* time-based releases; they are not scheduled or based on specific benchmarks,
statistics or completion of features. To gain confidence in a particular
release, it is strongly recommended that you start early by experimenting with release candidates and/or follow-up snapshots.

## Apereo Membership

If you benefit from Apereo CAS as free and open-source software, we invite you
to [join the Apereo Foundation](https://www.apereo.org/content/apereo-membership)
and financially support the project at a capacity that best suits your deployment. Note that all development activity is performed
*almost exclusively* on a voluntary basis with no expectations, commitments or strings attached. Having the financial means to better
sustain engineering activities will allow the developer community to allocate *dedicated and committed* time for long-term support,
maintenance and release planning, especially when it comes to addressing critical and security issues in a timely manner.

## Get Involved

- Start your CAS deployment today. Try out features and [share feedback](/cas/Mailing-Lists.html).
- Better yet, [contribute patches](/cas/developer/Contributor-Guidelines.html).
- Suggest and apply documentation improvements.

## Resources

- [Release Schedule](https://github.com/apereo/cas/milestones)
- [Release Policy](/cas/developer/Release-Policy.html)

## New & Noteworthy

The following items are new improvements and enhancements presented in this release.

### JDK Requirement

The JDK baseline requirement for this CAS release is and **MUST** be JDK `17`. All compatible distributions
such as Amazon Corretto, Zulu, Eclipse Temurin, etc should work and are implicitly supported.

### Spring Boot 3

The migration of the entire codebase to Spring Boot 3 and Jakarta APIs is ongoing, and at the moment
is waiting for the wider ecosystem of suppporting frameworks and libraries to catch up to these changes. 
As a quick status update, we anticipate the work to finalize in the next release candidate.

### Testing Strategy

The collection of end-to-end browser tests based on Puppeteer continue to grow to cover more use cases
and scenarios. At the moment, total number of jobs stands at approximately `366` distinct scenarios. The overall
test coverage of the CAS codebase is approximately `94%`.

### Account Registration

The [account registration functionality](../registration/Account-Registration-Overview.html) now allows user accounts
to be provisioned to [Apache Syncope](../registration/Account-Registration-Provisioning-Syncope.html).

### Inwebo Multifactor Authentication

Major improvements to [Inwebo Multifactor Authentication](../mfa/Inwebo-Authentication.html) to better detect authentication methods,
support Inwebo's virtual authenticator and provide better control over push/browser settings. The mAccessWeb enrollment is also
improved with a PIN code confirmation.

### OpenFGA Access Strategy

A new access strategy is now available to enforce fine-grained authorization 
requests based on [Auth0's OpenFGA](../services/Service-Access-Strategy-OpenFGA.html).
 
### Simple Multifactor Authentication

The [Simple Multifactor Authentication](../mfa/Simple-Multifactor-Authentication.html) module can now enable
a special actuator endpoint to allow REST clients to create tokens programmatically. This extension module is also enhanced
to support [REST-based authentication](../protocol/REST-Protocol-CredentialAuthentication.html) via a special `sotp` parameter, 
in scenarios where the request may require and/or trigger multifactor authentication.

### REST Authentication

Support for [REST authentication](../authentication/Rest-Authentication.html) is now improved to support
multiple REST endpoints and configuration blocks.

### OPA Access Strategy

A new access strategy is now available to enforce fine-grained authorization
requests based on [Open Policy Agent](../services/Service-Access-Strategy-OpenPolicyAgent.html).

### Duo Security Enrollment

If you would rather not rely on [Duo Security](../mfa/DuoSecurity-Authentication.html)’s built-in 
registration flow and have your own registration application 
that allows users to onboard and enroll with Duo Security, you can instruct CAS to redirect to your enrollment 
application, if the user’s account status is determined to require enrollment with a special `principal` parameter
that contains the user’s identity as JWT.

### OpenID Connect JARM

Initial support for [JWT Secured Authorization Response Mode](../authentication/OIDC-Authentication-JARM.html) is now available for OpenID Connect.
     
### Delegated Authentication Profile Selection

[Delegated authentication profile selection](../integration/Delegate-Authentication-ProfileSelection.html) can 
now support an LDAP directory to locate candidate linked profiles.

### Redis Ticket Registry

A series of performance improvements to the [Redis Ticket Registry](../ticketing/Redis-Ticket-Registry.html) 
to support synchronized caching and better key selection criteria across CAS server nodes in a clustered deployment. 

### DynamoDb Ticket Registry

When creating tickets, the [DynamoDb Ticket Registry](../ticketing/DynamoDb-Ticket-Registry.html) will adjust tables to enable auto-expiry of 
ticket objects. On a per-table basis, a special `expiration` attribute is assigned to the table which is the expiration time of the ticket 
in POSIX timestamp format. This attribute is automatically defined, calculated and populated for all ticket objects. Doing so should allow you
to turn off the ticket registry cleaner and let DynamoDb remove expired objects on its own. Furthermore, a number of performance 
improvements are now in place to support scanning, counting and updating ticket objects in DynamoDb using pagination and batch operations.

## Other Stuff

- Small adjustments to [attribute consent](../integration/Attribute-Release-Consent-Activation.html) rules when activated for and assigned to a specific 
  service definition. 
- Client secrets for [OpenID Connect Services](../authentication/OIDC-Authentication-Clients.html) are now URL-decoded before validations.
- A [DynamoDb-based health indicator](../monitoring/Configuring-Monitoring-DynamoDb.html) is available to report back on the health status of
  DynamoDb tables and connections.
- [Git service registry](../services/Git-Service-Management.html) is now able to support rebase operations.
- SSO sessions under [account profile](../registration/Account-Management-Overview.html) can now be selectively removed.
- Authentication attributes can now optionally be included in OpenID Connect ID token or user profile payloads. 
- The ability to secure actuator endpoints via subnet addresses is now restored.
- The persistence units for all JPA integrations are now corrected to refer to the defined unit name.
- Username providers [based on attributes](../integration/Attribute-Release-PrincipalId-Attribute.html) are now able to remove text from the final username 
  using regular expressions. 
- Performance improvements to [Redis ticket registry](../ticketing/Redis-Ticket-Registry.html), particularly around fetching tickets from Redis.

## Library Upgrades

- Spring Boot   
- Apache Tomcat
- Twilio
- Jose4j
- Gradle
- Apache Ignite
- Apache Shiro
- Netty
- Errorprone
- Jackson
- Hazelcast
- Lettuce
- Micrometer
- Nimbus
- InfluxDb
- WSS4j
- Hibernate
- Groovy
- HAL Explorer
- Swagger
- Jodatime
- Spring Data
- Azure CosmosDb
- MongoDb
