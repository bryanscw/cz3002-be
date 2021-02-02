package com.qwerty.cogbench.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

@Configuration
public class TokenStoreConfig {

  @Value("${spring.datasource.driverClassName}")
  private String driverClassName;

  @Value("${spring.datasource.url}")
  private String dataSrcUrl;

  @Value("${spring.datasource.username}")
  private String dataSrcUsername;

  @Value("${spring.datasource.password}")
  private String dataSrcPassword;

  @Bean
  public TokenStore tokenStore() {
    final DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setDriverClassName(driverClassName);
    dataSource.setUrl(dataSrcUrl);
    dataSource.setUsername(dataSrcUsername);
    dataSource.setPassword(dataSrcPassword);
    return new JdbcTokenStore(dataSource);
  }

  @Bean
  public DefaultTokenServices tokenServices() {
    DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
    defaultTokenServices.setTokenStore(tokenStore());
    defaultTokenServices.setSupportRefreshToken(true);
    return defaultTokenServices;
  }
}
