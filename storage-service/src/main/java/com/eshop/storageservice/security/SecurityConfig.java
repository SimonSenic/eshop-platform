package com.eshop.storageservice.security;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.eshop.storageservice.exception.AccessDeniedExceptionHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	@Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.authorizeHttpRequests().requestMatchers(GET, "/storage-service/products/**").permitAll();
        http.authorizeHttpRequests().requestMatchers(POST, "/storage-service/products/{id}/order").hasAnyAuthority("CUSTOMER");
        http.authorizeHttpRequests().requestMatchers("/storage-service/products/**").hasAnyAuthority("ADMIN");
        http.authorizeHttpRequests().requestMatchers("/**").permitAll();
        http.exceptionHandling().accessDeniedHandler(accessDeniedHandler());
        http.authorizeHttpRequests().anyRequest().authenticated();
        http.addFilterBefore(new AuthorizationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
	
	@Bean
	public AuditorAware<String> auditorAware(){
		return () -> Optional.of(SecurityContextHolder.getContext().getAuthentication().getName());
	}
	
	@Bean
	public AccessDeniedHandler accessDeniedHandler() {
		return new AccessDeniedExceptionHandler();
	}
	
}
