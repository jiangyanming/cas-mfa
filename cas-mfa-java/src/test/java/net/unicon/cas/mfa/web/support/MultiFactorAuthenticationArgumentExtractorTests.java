package net.unicon.cas.mfa.web.support;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.jasig.cas.web.support.ArgumentExtractor;
import org.jasig.cas.web.support.CasArgumentExtractor;
import org.jasig.cas.web.support.SamlArgumentExtractor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(JUnit4.class)
public class MultiFactorAuthenticationArgumentExtractorTests {

    private final List<ArgumentExtractor> supportedArgumentExtractors;

    public MultiFactorAuthenticationArgumentExtractorTests() {
        this.supportedArgumentExtractors = new ArrayList<ArgumentExtractor>();
        this.supportedArgumentExtractors.add(new CasArgumentExtractor());
        this.supportedArgumentExtractors.add(new SamlArgumentExtractor());
    }
    /**
     * When login presents no authentication method, the extractor extracts a null service.
     */
    @Test
    public void testMissingAuthenticationMethodParameterYieldsNullService() {

        // let's say we support all sorts of interesting authentication methods,
        // but this login request isn't going to require any of these
        final List<String> supportedAuthenticationMethods =
                Arrays.asList("fingerprint", "strong_two_factor", "personal_attestation", "retina_scan");

        final MultiFactorAuthenticationArgumentExtractor extractor =
                new MultiFactorAuthenticationArgumentExtractor(supportedAuthenticationMethods, this.supportedArgumentExtractors);

        final HttpServletRequest request = mock(HttpServletRequest.class);

        when(request.getParameter("service")).thenReturn("https://www.github.com");

        // let's say the authn_method request parameter is missing outright
        when(request.getParameter(MultiFactorAuthenticationSupportingWebApplicationService.CONST_PARAM_AUTHN_METHOD))
                .thenReturn(null);

        assertNull(extractor.extractService(request));
    }


    /**
     * When login presents an unrecognized authentication method, the extractor extracts a null service.
     */
    @Test(expected=UnrecognizedAuthenticationMethodException.class)
    public void testUnrecognizedAuthenticationMethodParameterYieldsNullService() {
        final List<String> emptyList = Collections.emptyList();
        final MultiFactorAuthenticationArgumentExtractor extractor =
                new MultiFactorAuthenticationArgumentExtractor(emptyList, this.supportedArgumentExtractors);

        final HttpServletRequest request = mock(HttpServletRequest.class);

        when(request.getParameter("service")).thenReturn("https://www.github.com");
        when(request.getParameter(MultiFactorAuthenticationSupportingWebApplicationService.CONST_PARAM_AUTHN_METHOD))
                .thenReturn("unrecognized_authentication_method");

        extractor.extractService(request);
    }

    /**
     * When login presents the one recognized authentication method, extractor extracts a service conveying the
     * required authentication method.
     */
    @Test
    public void testRecognizedAuthenticationMethodParameterYieldsAuthenticationMethodRequiringService() {
        final List<String> validAuthenticationMethods = Arrays.asList("strong_two_factor");
        final MultiFactorAuthenticationArgumentExtractor extractor =
                new MultiFactorAuthenticationArgumentExtractor(validAuthenticationMethods
                        , this.supportedArgumentExtractors);

        final HttpServletRequest request = mock(HttpServletRequest.class);

        when(request.getParameter("service")).thenReturn("https://www.github.com");
        when(request.getParameter(MultiFactorAuthenticationSupportingWebApplicationService.CONST_PARAM_AUTHN_METHOD))
                .thenReturn("strong_two_factor");

        assertTrue(extractor.extractService(request) instanceof MultiFactorAuthenticationSupportingWebApplicationService);

        MultiFactorAuthenticationSupportingWebApplicationService authenticationMethodRequiringService =
                (MultiFactorAuthenticationSupportingWebApplicationService) extractor.extractService(request);

        assertEquals("strong_two_factor", authenticationMethodRequiringService.getAuthenticationMethod());
    }

    /**
     * When login presents a recognized authentication method among several supported methods,
     * extractor extracts a service conveying the required authentication method.
     */
    @Test
    public void testRecognizedAuthenticationMethodParamAmongMultipleSupportedYieldsService() {

        // this is a bit of testing paranoia, but always want to check that one item isn't an edge case
        final List<String> validAuthenticationMethods =
                Arrays.asList("fingerprint", "strong_two_factor", "personal_attestation", "retina_scan");

        final MultiFactorAuthenticationArgumentExtractor extractor =
                new MultiFactorAuthenticationArgumentExtractor(validAuthenticationMethods, this.supportedArgumentExtractors);

        final HttpServletRequest request = mock(HttpServletRequest.class);

        when(request.getParameter("service")).thenReturn("https://www.github.com");
        when(request.getParameter(MultiFactorAuthenticationSupportingWebApplicationService.CONST_PARAM_AUTHN_METHOD))
                .thenReturn("personal_attestation");

        assertTrue(extractor.extractService(request) instanceof MultiFactorAuthenticationSupportingWebApplicationService);

        MultiFactorAuthenticationSupportingWebApplicationService authenticationMethodRequiringService =
                (MultiFactorAuthenticationSupportingWebApplicationService) extractor.extractService(request);

        assertEquals("personal_attestation", authenticationMethodRequiringService.getAuthenticationMethod());
    }

    /**
     * When login presents no service parameter, extractor extracts a null service.
     */
    @Test
    public void testMissingServiceParameterYieldsNullService() {

        // this is a bit of testing paranoia, but always want to check that one item isn't an edge case
        final List<String> validAuthenticationMethods =
                Arrays.asList("fingerprint", "strong_two_factor", "personal_attestation", "retina_scan");

        final MultiFactorAuthenticationArgumentExtractor extractor =
                new MultiFactorAuthenticationArgumentExtractor(validAuthenticationMethods, this.supportedArgumentExtractors);

        final HttpServletRequest request = mock(HttpServletRequest.class);

        when(request.getParameter("service")).thenReturn(null);
        when(request.getParameter(MultiFactorAuthenticationSupportingWebApplicationService.CONST_PARAM_AUTHN_METHOD))
                .thenReturn("personal_attestation");

        assertNull(extractor.extractService(request));

    }

    @Test
    public void testRecognizedAuthenticationMethodParameterInSamlRequest() {
        final List<String> validAuthenticationMethods = Arrays.asList("strong_two_factor");
        final MultiFactorAuthenticationArgumentExtractor extractor =
                new MultiFactorAuthenticationArgumentExtractor(validAuthenticationMethods
                        , this.supportedArgumentExtractors);

        final HttpServletRequest request = mock(HttpServletRequest.class);

        when(request.getParameter("TARGET")).thenReturn("https://www.github.com");
        when(request.getParameter(MultiFactorAuthenticationSupportingWebApplicationService.CONST_PARAM_AUTHN_METHOD))
                .thenReturn("strong_two_factor");

        assertTrue(extractor.extractService(request) instanceof MultiFactorAuthenticationSupportingWebApplicationService);

        MultiFactorAuthenticationSupportingWebApplicationService authenticationMethodRequiringService =
                (MultiFactorAuthenticationSupportingWebApplicationService) extractor.extractService(request);

        assertEquals("strong_two_factor", authenticationMethodRequiringService.getAuthenticationMethod());
    }
}
