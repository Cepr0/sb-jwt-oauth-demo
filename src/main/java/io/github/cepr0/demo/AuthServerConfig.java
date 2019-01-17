package io.github.cepr0.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@EnableAuthorizationServer
@EnableResourceServer
public class AuthServerConfig extends AuthorizationServerConfigurerAdapter {

	public static final String TOKEN_KEY = "abracadabra";

	private final AuthenticationManager authenticationManager;

	public AuthServerConfig(final AuthenticationManager authenticationManager) {
		this.authenticationManager = authenticationManager;
	}

	@Override
	public void configure(ClientDetailsServiceConfigurer clientDetailsService) throws Exception {
		clientDetailsService.inMemory()
				.withClient("client")
				.secret("{noop}")
				.scopes("*")
				.authorizedGrantTypes("password", "refresh_token")
				.accessTokenValiditySeconds(60 * 2) // 2 min
				.refreshTokenValiditySeconds(60 * 60); // 60 min
	}
	
	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
		TokenEnhancerChain chain = new TokenEnhancerChain();
		chain.setTokenEnhancers(List.of(tokenEnhancer(), tokenConverter()));
		endpoints
				.tokenStore(tokenStore())
				.reuseRefreshTokens(false)
				.tokenEnhancer(chain)
				.authenticationManager(authenticationManager);
	}

	@Bean
	public TokenStore tokenStore() {
		return new JwtTokenStore(tokenConverter());
	}

	@Bean
	public JwtAccessTokenConverter tokenConverter() {
		JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
		converter.setSigningKey(TOKEN_KEY);
		converter.setAccessTokenConverter(authExtractor());
		return converter;
	}
	
	private TokenEnhancer tokenEnhancer() {
		return (accessToken, authentication) -> {
				if (authentication != null && authentication.getPrincipal() instanceof AuthUser) {
					AuthUser authUser = (AuthUser) authentication.getPrincipal();
					Map<String, Object> additionalInfo = new HashMap<>();
					additionalInfo.put("user_email", authUser.getEmail());
					((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);
				}
				return accessToken;
			};
	}

	@Bean
	public DefaultAccessTokenConverter authExtractor() {
		return new DefaultAccessTokenConverter() {
			@Override
			public OAuth2Authentication extractAuthentication(Map<String, ?> claims) {
				OAuth2Authentication authentication = super.extractAuthentication(claims);
				authentication.setDetails(claims);
				return authentication;
			}
		};
	}
}