package com.eshop.paymentservice.security;

import static org.springframework.http.HttpMethod.POST;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.eshop.paymentservice.exception.AccessDeniedExceptionHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	@Autowired
	private Environment environment;
	
	@Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.authorizeHttpRequests().requestMatchers(POST, "/payment-service/payment/{orderId}").hasAnyAuthority("CUSTOMER");
        http.authorizeHttpRequests().requestMatchers("/swagger-ui/**").permitAll();
        http.authorizeHttpRequests().requestMatchers("/v3/api-docs/**").permitAll();
        http.authorizeHttpRequests().requestMatchers("/actuator/**").permitAll();
        http.exceptionHandling().accessDeniedHandler(accessDeniedHandler());
        http.authorizeHttpRequests().anyRequest().authenticated();
        http.addFilterBefore(new AuthorizationFilter(environment), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
	
	@Bean
	public AccessDeniedHandler accessDeniedHandler() {
		return new AccessDeniedExceptionHandler();
	}
	
}
