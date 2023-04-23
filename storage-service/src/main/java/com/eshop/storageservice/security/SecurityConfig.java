package com.eshop.storageservice.security;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.IpAddressMatcher;

import com.eshop.storageservice.exception.AccessDeniedExceptionHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	@Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.authorizeHttpRequests().requestMatchers(GET, "/storage-service/products/**").permitAll();
        http.authorizeHttpRequests().requestMatchers(POST, "/storage-service/products/{id}/order").hasAnyAuthority("CUSTOMER");
        http.authorizeHttpRequests().requestMatchers(PUT, "/storage-service/products/{id}/update-availability").access(hasIpAddress("192.168.100.227"));
        http.authorizeHttpRequests().requestMatchers("/storage-service/products/**").hasAnyAuthority("ADMIN");
        //http.authorizeHttpRequests().requestMatchers("/**").permitAll();
        http.exceptionHandling().accessDeniedHandler(accessDeniedHandler());
        http.authorizeHttpRequests().anyRequest().authenticated();
        http.addFilterBefore(new AuthorizationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
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
