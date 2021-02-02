package com.qwerty.cogbench.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;

@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

  @Value("${oauth2.client-id}")
  private String oauthClientId;

  @Value("${oauth2.client-secret}")
  private String oauthClientSecret;

  @Value("${oauth2.grant-type}")
  private String grantType;

  @Value("${oauth2.authorization-code}")
  private String authorizationCode;

  @Value("${oauth2.refresh-token}")
  private String refreshToken;

  @Value("${oauth2.scope-read}")
  private String scopeRead;

  @Value("${oauth2.scope-write}")
  private String scopeWrite;

  @Value("${oauth2.scope-trust}")
  private String scopeTrust;

  @Value("${oauth2.access-token-validity-seconds}")
  private int accessTokenValiditySeconds;

  @Value("${oauth2.refresh-token-validity-seconds}")
  private int refreshTokenValiditySeconds;

  @Autowired
  private AuthenticationManager authManager;

  @Autowired
  private TokenStore tokenStore;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Override
  public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
    clients
        .inMemory()
        .withClient(oauthClientId)
        .secret(passwordEncoder.encode(oauthClientSecret))
        .authorizedGrantTypes(grantType, authorizationCode, refreshToken)
        .scopes(scopeRead, scopeWrite, scopeTrust)
        .accessTokenValiditySeconds(accessTokenValiditySeconds)
        .refreshTokenValiditySeconds(refreshTokenValiditySeconds);
  }

  /**
   * Defines the authorization and token endpoints and the token services
   *
   * @param endpoints
   */
  @Override
  public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
    endpoints.tokenStore(tokenStore)
        .authenticationManager(authManager);
  }

}
