package org.apereo.cas.web.flow;

import org.apereo.cas.api.PasswordlessAuthenticationRequest;
import org.apereo.cas.api.PasswordlessTokenRepository;
import org.apereo.cas.api.PasswordlessUserAccount;
import org.apereo.cas.impl.token.PasswordlessAuthenticationToken;

import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.engine.Flow;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockFlowExecutionContext;
import org.springframework.webflow.test.MockFlowSession;
import org.springframework.webflow.test.MockRequestContext;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link AcceptPasswordlessAuthenticationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@Import(BaseWebflowConfigurerTests.SharedTestConfiguration.class)
@Tag("WebflowAuthenticationActions")
@TestPropertySource(properties = "cas.authn.passwordless.accounts.simple.casuser=casuser@example.org")
public class AcceptPasswordlessAuthenticationActionTests extends BasePasswordlessAuthenticationActionTests {
    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_ACCEPT_PASSWORDLESS_AUTHN)
    private Action acceptPasswordlessAuthenticationAction;

    @Autowired
    @Qualifier(PasswordlessTokenRepository.BEAN_NAME)
    private PasswordlessTokenRepository passwordlessTokenRepository;

    @Test
    public void verifyAction() throws Exception {
        val exec = new MockFlowExecutionContext(new MockFlowSession(new Flow(CasWebflowConfigurer.FLOW_ID_LOGIN)));
        val context = new MockRequestContext(exec);

        putAccountInto(context);
        val token = createToken();

        val request = new MockHttpServletRequest();
        request.addParameter("token", token.getToken());
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, acceptPasswordlessAuthenticationAction.execute(context).getId());
        assertTrue(passwordlessTokenRepository.findToken("casuser").isEmpty());
    }

    private PasswordlessAuthenticationToken createToken() {
        val passwordlessUserAccount = PasswordlessUserAccount.builder().username("casuser").build();
        val passwordlessRequest = PasswordlessAuthenticationRequest.builder().username("casuser").build();
        val token = passwordlessTokenRepository.createToken(passwordlessUserAccount, passwordlessRequest);
        passwordlessTokenRepository.saveToken(passwordlessUserAccount, passwordlessRequest, token);
        return token;
    }

    @Test
    public void verifyUnknownToken() throws Exception {
        val exec = new MockFlowExecutionContext(new MockFlowSession(new Flow(CasWebflowConfigurer.FLOW_ID_LOGIN)));
        val context = new MockRequestContext(exec);

        putAccountInto(context);
        createToken();
        
        val request = new MockHttpServletRequest();
        request.addParameter("token", UUID.randomUUID().toString());

        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, acceptPasswordlessAuthenticationAction.execute(context).getId());
    }

    @Test
    public void verifyMissingTokenAction() throws Exception {
        val exec = new MockFlowExecutionContext(new MockFlowSession(new Flow(CasWebflowConfigurer.FLOW_ID_LOGIN)));
        val context = new MockRequestContext(exec);
        val request = new MockHttpServletRequest();

        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        putAccountInto(context);
        assertEquals(CasWebflowConstants.TRANSITION_ID_AUTHENTICATION_FAILURE, acceptPasswordlessAuthenticationAction.execute(context).getId());
    }

    private static PasswordlessUserAccount putAccountInto(final MockRequestContext context) {
        val account = PasswordlessUserAccount.builder()
            .email("email")
            .phone("phone")
            .username("casuser")
            .name("casuser")
            .build();
        PasswordlessWebflowUtils.putPasswordlessAuthenticationAccount(context, account);
        return account;
    }
}
