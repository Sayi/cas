package org.apereo.cas.web.flow.login;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.util.spring.beans.BeanSupplier;
import org.apereo.cas.web.flow.actions.BaseCasWebflowAction;
import org.apereo.cas.web.flow.decorator.WebflowDecorator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link RenderLoginAction}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiredArgsConstructor
@Slf4j
public class RenderLoginAction extends BaseCasWebflowAction {
    /**
     * The services manager with access to the registry.
     **/
    protected final ServicesManager servicesManager;

    /**
     * Collection of CAS settings.
     */
    protected final CasConfigurationProperties casProperties;

    /**
     * The current application context.
     */
    protected final ApplicationContext applicationContext;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        applicationContext.getBeansOfType(WebflowDecorator.class)
            .values()
            .stream()
            .filter(BeanSupplier::isNotProxy)
            .sorted()
            .forEach(decorator -> decorator.decorate(requestContext, applicationContext));
        return null;
    }
}
