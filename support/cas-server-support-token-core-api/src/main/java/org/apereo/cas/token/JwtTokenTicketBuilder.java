package org.apereo.cas.token;

import org.apereo.cas.authentication.ProtocolAttributeEncoder;
import org.apereo.cas.authentication.principal.WebApplicationService;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.TicketGrantingTicket;
import org.apereo.cas.ticket.TicketValidator;
import org.apereo.cas.util.DateTimeUtils;
import org.apereo.cas.util.function.FunctionUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This is {@link JwtTokenTicketBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Getter
@RequiredArgsConstructor
@Slf4j
public class JwtTokenTicketBuilder implements TokenTicketBuilder {
    private final TicketValidator ticketValidator;

    private final ExpirationPolicyBuilder expirationPolicy;

    private final JwtBuilder jwtBuilder;

    private final ServicesManager servicesManager;

    private final CasConfigurationProperties casProperties;

    @Override
    public String build(final String serviceTicketId, final WebApplicationService webApplicationService) {
        val assertion = FunctionUtils.doUnchecked(() -> ticketValidator.validate(serviceTicketId, webApplicationService.getId()));
        val attributes = new LinkedHashMap(assertion.getAttributes());
        attributes.putAll(assertion.getPrincipal().getAttributes());

        LOGGER.trace("Assertion attributes received are [{}]", attributes);
        val registeredService = servicesManager.findServiceBy(webApplicationService);
        val finalAttributes = ProtocolAttributeEncoder.decodeAttributes(attributes, registeredService, webApplicationService);
        LOGGER.debug("Final attributes decoded are [{}]", finalAttributes);

        val dt = ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(getTimeToLive());
        val validUntilDate = DateTimeUtils.dateOf(dt);
        val builder = JwtBuilder.JwtRequest.builder();
        val request = builder
            .registeredService(Optional.ofNullable(registeredService))
            .serviceAudience(webApplicationService.getId())
            .issueDate(new Date())
            .jwtId(serviceTicketId)
            .subject(assertion.getPrincipal().getId())
            .validUntilDate(validUntilDate)
            .attributes(finalAttributes)
            .issuer(casProperties.getServer().getPrefix())
            .build();
        LOGGER.debug("Building JWT using [{}]", request);
        return jwtBuilder.build(request);
    }

    @Override
    public String build(final TicketGrantingTicket ticketGrantingTicket, final Map<String, List<Object>> claims) {
        val authentication = ticketGrantingTicket.getAuthentication();

        val attributes = new HashMap<>(authentication.getAttributes());
        attributes.putAll(authentication.getPrincipal().getAttributes());
        attributes.putAll(claims);

        val dt = ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(getTimeToLive());
        val validUntilDate = DateTimeUtils.dateOf(dt);

        val builder = JwtBuilder.JwtRequest.builder();
        val request = builder
            .serviceAudience(casProperties.getServer().getPrefix())
            .registeredService(Optional.empty())
            .issueDate(DateTimeUtils.dateOf(ticketGrantingTicket.getCreationTime()))
            .jwtId(ticketGrantingTicket.getId())
            .subject(authentication.getPrincipal().getId())
            .validUntilDate(validUntilDate)
            .attributes(attributes)
            .issuer(casProperties.getServer().getPrefix())
            .build();
        return jwtBuilder.build(request);
    }

    /**
     * Gets time to live.
     *
     * @return the time to live
     */
    protected Long getTimeToLive() {
        val timeToLive = expirationPolicy.buildTicketExpirationPolicy().getTimeToLive();
        return Long.MAX_VALUE == timeToLive ? Long.valueOf(Integer.MAX_VALUE) : timeToLive;
    }
}
