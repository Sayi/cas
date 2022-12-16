package org.apereo.cas.web.flow;

import org.apereo.cas.api.PasswordlessRequestParser;
import org.apereo.cas.api.PasswordlessUserAccountStore;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.web.support.WebUtils;

import lombok.val;
import org.springframework.webflow.action.EventFactorySupport;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

/**
 * This is {@link VerifyPasswordlessAccountAuthenticationAction}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
public class VerifyPasswordlessAccountAuthenticationAction extends BasePasswordlessCasWebflowAction {
    private final PasswordlessUserAccountStore passwordlessUserAccountStore;
    private final PasswordlessRequestParser passwordlessRequestParser;

    public VerifyPasswordlessAccountAuthenticationAction(final CasConfigurationProperties casProperties,
                                                         final PasswordlessUserAccountStore passwordlessUserAccountStore,
                                                         final PasswordlessRequestParser passwordlessRequestParser) {
        super(casProperties);
        this.passwordlessUserAccountStore = passwordlessUserAccountStore;
        this.passwordlessRequestParser = passwordlessRequestParser;
    }

    @Override
    public Event doExecute(final RequestContext requestContext) {
        val username = requestContext.getRequestParameters().getRequired(PasswordlessRequestParser.PARAMETER_USERNAME);
        val passwordlessRequest = passwordlessRequestParser.parse(username);
        val account = passwordlessUserAccountStore.findUser(passwordlessRequest.getUsername());
        if (account.isEmpty()) {
            WebUtils.addErrorMessageToContext(requestContext, "passwordless.error.unknown.user");
            return error();
        }
        val user = account.get();
        if (user.isRequestPassword()) {
            PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(requestContext, user);
            val isDelegationActive = isDelegatedAuthenticationActiveFor(requestContext, user);
            DelegationWebflowUtils.putDelegatedAuthenticationDisabled(requestContext, !isDelegationActive);
            return new EventFactorySupport().event(this, CasWebflowConstants.TRANSITION_ID_PROMPT);
        }
        PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(requestContext, user);
        PasswordlessWebflowUtils.putPasswordlessAuthenticationRequest(requestContext, passwordlessRequest);
        return success();
    }
}
