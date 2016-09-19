package org.pac4j.demo.j2e;

import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.client.CasProxyReceptor;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.core.authorization.authorizer.IsAnonymousAuthorizer;
import org.pac4j.core.authorization.authorizer.IsAuthenticatedAuthorizer;
import org.pac4j.core.authorization.authorizer.RequireAnyRoleAuthorizer;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.direct.AnonymousClient;
import org.pac4j.core.config.Config;
import org.pac4j.core.config.ConfigFactory;
import org.pac4j.core.matching.ExcludedPathMatcher;
import org.pac4j.http.client.direct.DirectBasicAuthClient;
import org.pac4j.http.client.direct.ParameterClient;
import org.pac4j.http.credentials.authenticator.test.SimpleTestUsernamePasswordAuthenticator;
import org.pac4j.jwt.credentials.authenticator.JwtAuthenticator;
import org.pac4j.oidc.client.GoogleOidcClient;
import org.pac4j.oidc.config.OidcConfiguration;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.client.SAML2ClientConfiguration;

import java.io.File;
import org.pac4j.cas.config.CasProtocol;

public class DemoConfigFactory implements ConfigFactory {

    @Override
    public Config build() {
        final OidcConfiguration oidcConfiguration = new OidcConfiguration();
        oidcConfiguration.setClientId("167480702619-8e1lo80dnu8bpk3k0lvvj27noin97vu9.apps.googleusercontent.com");
        oidcConfiguration.setSecret("MhMme_Ik6IH2JMnAT6MFIfee");
        oidcConfiguration.setUseNonce(true);
        //oidcClient.setPreferredJwsAlgorithm(JWSAlgorithm.RS256);
        oidcConfiguration.addCustomParam("prompt", "consent");
        final GoogleOidcClient oidcClient = new GoogleOidcClient(oidcConfiguration);
        oidcClient.setAuthorizationGenerator(profile -> profile.addRole("ROLE_ADMIN"));

        final SAML2ClientConfiguration cfg = new SAML2ClientConfiguration("resource:samlKeystore.jks",
                                                "pac4j-demo-passwd",
                                                "pac4j-demo-passwd",
                                                "resource:testshib-providers.xml");
        cfg.setMaximumAuthenticationLifetime(3600);
        cfg.setServiceProviderEntityId("http://localhost:8080/callback?client_name=SAML2Client");
        cfg.setServiceProviderMetadataPath(new File("sp-metadata.xml").getAbsolutePath());
        final SAML2Client saml2Client = new SAML2Client(cfg);

        // CAS
        final CasConfiguration configuration = new CasConfiguration("https://glast-ground.slac.stanford.edu/cas-srs/login", CasProtocol.CAS20);
        
        // IF we update cas...
        //final CasConfiguration configuration = new CasConfiguration("https://glast-ground.slac.stanford.edu/cas-srs/login);
        //final CasProxyReceptor casProxy = new CasProxyReceptor();
        //configuration.setProxyReceptor(casProxy);
        final CasClient casClient = new CasClient(configuration);

        /*final DirectCasClient casClient = new DirectCasClient(configuration);
        casClient.setName("CasClient");*/

        // REST authent with JWT for a token passed in the url as the token parameter
        ParameterClient parameterClient = new ParameterClient("token", new JwtAuthenticator(Constants.JWT_SALT));
        parameterClient.setSupportGetRequest(true);
        parameterClient.setSupportPostRequest(false);

        // basic auth
        final DirectBasicAuthClient directBasicAuthClient = new DirectBasicAuthClient(new SimpleTestUsernamePasswordAuthenticator());

        final Clients clients = new Clients("http://localhost:8080/callback", oidcClient, saml2Client, casClient, parameterClient,
                directBasicAuthClient, new AnonymousClient()); //, casProxy);

        final Config config = new Config(clients);
        config.addAuthorizer("admin", new RequireAnyRoleAuthorizer<>("ROLE_ADMIN"));
        config.addAuthorizer("custom", new CustomAuthorizer());
        config.addAuthorizer("mustBeAnon", new IsAnonymousAuthorizer<>("/?mustBeAnon"));
        config.addAuthorizer("mustBeAuth", new IsAuthenticatedAuthorizer<>("/?mustBeAuth"));
        config.addMatcher("excludedPath", new ExcludedPathMatcher("^/facebook/notprotected\\.jsp$"));
        return config;
    }
}
