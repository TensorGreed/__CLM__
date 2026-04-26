package com.clm.platform.config;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.clm.platform.domain.serviceaccount.ApiTokenAuthenticationFilter;
import com.clm.platform.security.OidcGroupAuthoritiesMapper;
import com.clm.platform.security.OidcProperties;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

	@Bean
	SecurityFilterChain securityFilterChain(
			HttpSecurity http,
			AuthenticationEntryPoint authenticationEntryPoint,
			AccessDeniedHandler accessDeniedHandler,
			ApiTokenAuthenticationFilter apiTokenAuthenticationFilter,
			OidcProperties oidcProperties,
			OidcGroupAuthoritiesMapper oidcGroupAuthoritiesMapper) throws Exception {
		http.cors(withDefaults())
			.csrf(AbstractHttpConfigurer::disable)
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(basic -> basic.authenticationEntryPoint(authenticationEntryPoint))
			.addFilterBefore(apiTokenAuthenticationFilter, BasicAuthenticationFilter.class)
			.exceptionHandling(exceptions -> exceptions
				.authenticationEntryPoint(authenticationEntryPoint)
				.accessDeniedHandler(accessDeniedHandler))
			.authorizeHttpRequests(authorize -> authorize
				.requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
				.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
				.requestMatchers(HttpMethod.GET, "/api/v1", "/api/v1/bootstrap/status").permitAll()
				.requestMatchers(HttpMethod.POST, "/api/v1/bootstrap/admin").permitAll()
				.anyRequest().authenticated());

		if (oidcProperties.enabled()) {
			http.oauth2Login(oauth2 -> oauth2
				.userInfoEndpoint(userInfo -> userInfo.userAuthoritiesMapper(oidcGroupAuthoritiesMapper::mapAuthorities)))
				.logout(withDefaults())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED));
		}
		else {
			http.oauth2Login(AbstractHttpConfigurer::disable)
				.logout(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
		}

		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	FilterRegistrationBean<ApiTokenAuthenticationFilter> apiTokenAuthenticationFilterRegistration(
			ApiTokenAuthenticationFilter filter) {
		FilterRegistrationBean<ApiTokenAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
		registration.setEnabled(false);
		return registration;
	}
}
