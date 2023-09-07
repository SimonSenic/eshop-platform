package com.eshop.userservice.security;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PUT;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.IpAddressMatcher;

import com.eshop.userservice.exception.AccessDeniedExceptionHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private Environment environment;
	
	@Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.authorizeHttpRequests().requestMatchers("/user-service/customer/**").permitAll();
        http.authorizeHttpRequests().requestMatchers(POST, "/user-service/admin/create-admin").hasAnyAuthority("ADMIN");
        http.authorizeHttpRequests().requestMatchers(PUT, "/user-service/admin/complete-registration").permitAll();
        http.authorizeHttpRequests().requestMatchers(GET, "/user-service/admin/get-user/{id}").access(hasIpAddress("192.168.100.227"));
        http.authorizeHttpRequests().requestMatchers(POST, "/user-service/user/login").permitAll();
        http.authorizeHttpRequests().requestMatchers("/user-service/user/**").hasAnyAuthority("ADMIN", "CUSTOMER");
        http.authorizeHttpRequests().requestMatchers("/swagger-ui/**").permitAll();
        http.authorizeHttpRequests().requestMatchers("/v3/api-docs/**").permitAll();
        http.authorizeHttpRequests().requestMatchers("/actuator/**").permitAll();
        http.exceptionHandling().accessDeniedHandler(accessDeniedHandler());
        http.authorizeHttpRequests().anyRequest().authenticated();
        http.addFilterBefore(new AuthenticationFilter(authenticationManager, environment), UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(new AuthorizationFilter(environment), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
	
	@Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
	
	@Bean
	public AuditorAware<String> auditorAware(){
		return () -> {
		    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		    return Optional.ofNullable(auth).map(temp -> temp.getName());
		};
	}
	
	@Bean
	public AccessDeniedHandler accessDeniedHandler() {
		return new AccessDeniedExceptionHandler();
	}
	
	@Bean
	public AuthorizationManager<RequestAuthorizationContext> hasIpAddress(String address) {
        IpAddressMatcher ipMatcher = new IpAddressMatcher(address);
        return (authentication, context) -> new AuthorizationDecision(ipMatcher.matches(context.getRequest()));
    }
	
}
