package com.eshop.orderservice.security;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;

import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.eshop.orderservice.exception.AccessDeniedExceptionHandler;

import feign.RequestInterceptor;
import feign.RequestTemplate;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	
	@Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.authorizeHttpRequests().requestMatchers(GET, "/order-service/orders").hasAnyAuthority("ADMIN");
        http.authorizeHttpRequests().requestMatchers(GET, "/order-service/orders/my").hasAnyAuthority("CUSTOMER");
        http.authorizeHttpRequests().requestMatchers(GET, "/order-service/orders/{id}").hasAnyAuthority("ADMIN", "CUSTOMER");
        http.authorizeHttpRequests().requestMatchers(PATCH, "/order-service/orders/{id}/process").hasAnyAuthority("ADMIN");
        http.authorizeHttpRequests().requestMatchers("/order-service/orders/**").hasAnyAuthority("CUSTOMER");
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
    public RequestInterceptor bearerTokenRequestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
            	if(!template.headers().containsKey("Authorization") && !template.url().contains("/update-availability")) {
            		template.header("Authorization", ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
            				.getRequest().getHeader("Authorization"));
            	}
            }
        };
    }
	
}
