package io.syndesis.simulator.security;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.DefaultOAuth2RefreshToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.AuthorizationRequest;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;

/**
 * @author Christoph Deppisch
 */
@Configuration
@EnableAuthorizationServer
@EnableConfigurationProperties(SimulatorOAuthClientProperties.class)
public class AuthenticationServerConfig extends AuthorizationServerConfigurerAdapter {

    private final SimulatorOAuthClientProperties clientProperties;

    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthenticationServerConfig(SimulatorOAuthClientProperties clientProperties, AuthenticationManager authenticationManager) {
        this.clientProperties = clientProperties;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) {
        oauthServer
            .tokenKeyAccess("permitAll()")
            .checkTokenAccess("isAuthenticated()");
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients)
            throws Exception {
        clients.inMemory()
                .withClient(clientProperties.getId())
                .secret(clientProperties.getSecret())
                .authorizedGrantTypes(clientProperties.getAuthorizedGrantTypes().toArray(new String[]{}))
                .authorities(clientProperties.getAuthorities().toArray(new String[]{}))
                .accessTokenValiditySeconds(clientProperties.getAccessTokenValiditySeconds())
                .refreshTokenValiditySeconds(clientProperties.getRefreshTokenValiditySeconds())
                .scopes(clientProperties.getScope().toArray(new String[]{}))
                .autoApprove(true);
    }

    @Bean
    public TokenStore tokenStore() {
        InMemoryTokenStore tokenStore = new InMemoryTokenStore();
        AuthorizationRequest authorizationRequest = new AuthorizationRequest();
        authorizationRequest.setClientId(clientProperties.getId());
        authorizationRequest.setAuthorities(clientProperties.getAuthorities().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
        authorizationRequest.setApproved(true);

        OAuth2Authentication authentication = new OAuth2Authentication(authorizationRequest.createOAuth2Request(), null);

        tokenStore.storeAccessToken(new DefaultOAuth2AccessToken(clientProperties.getAccessToken()), authentication);
        tokenStore.storeRefreshToken(new DefaultOAuth2RefreshToken(clientProperties.getRefreshToken()), authentication);
        return tokenStore;
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
        endpoints
            .tokenStore(tokenStore())
            .authenticationManager(authenticationManager);
    }
}