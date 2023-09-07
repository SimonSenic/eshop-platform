package com.eshop.notificationservice.service;

import java.util.Date;

import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.eshop.notificationservice.dto.OrderDTO;
import com.eshop.notificationservice.dto.Role;
import com.eshop.notificationservice.dto.UserDTO;
import com.eshop.notificationservice.exception.BusinessException;

import io.github.resilience4j.retry.annotation.Retry;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class NotificationService {
	private final JavaMailSender mailSender;
	private final TemplateEngine templateEngine;
	private final ApiClient apiClient;
	private final Environment environment;
	
	public void log(String message) {
		log.info(message);
	}
	
	@Retry(fallbackMethod = "servicesDownFallback", name = "")
	public void sendConfirmRegistratonEmail(Long userId) throws MessagingException {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		UserDTO user = apiClient.getUser(userId).getBody();
		
		helper.setTo(user.getEmail());
		helper.setSubject("Eshop Platform - Confirm registration");
		Context context = new Context();
        context.setVariable("username", user.getUsername());
        context.setVariable("link", "http://localhost:9090/user-service/customer/confirm-registration?verificationToken=" +generateVerificationToken(user));
        String body = templateEngine.process("confirm-registration-template", context);
		helper.setText(body, true);
		mailSender.send(message);
	}
	
	@Retry(fallbackMethod = "servicesDownFallback", name = "")
	public void sendCompleteRegistratonEmail(Long userId) throws MessagingException {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		UserDTO user = apiClient.getUser(userId).getBody();
		
		helper.setTo(user.getEmail());
		helper.setSubject("Eshop Platform - Complete registration");
		Context context = new Context();
        context.setVariable("link", "http://localhost:9090/user-service/admin/complete-registration?verificationToken=" +generateVerificationToken(user));
        String body = templateEngine.process("complete-registration-template", context);
		helper.setText(body, true);
		mailSender.send(message);
	}
	
	@Retry(fallbackMethod = "servicesDownFallback", name = "")
	public void sendPasswordRecoveryEmail(Long userId) throws MessagingException {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		UserDTO user = apiClient.getUser(userId).getBody();
		
		helper.setTo(user.getEmail());
		helper.setSubject("Eshop Platform - Recover password");
		Context context = new Context();
		context.setVariable("username", user.getUsername());
        context.setVariable("link", "http://localhost:9090/user-service/user/set-new-password?verificationToken=" +generateVerificationToken(user));
        String body = templateEngine.process("password-recovery-template", context);
		helper.setText(body, true);
		mailSender.send(message);
	}
	
	@Retry(fallbackMethod = "servicesDownFallback", name = "")
	public void sendPaymentConfirmationEmail(Long orderId) throws MessagingException {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		OrderDTO order = apiClient.getOrder(orderId).getBody();
		UserDTO user = apiClient.getUser(order.getUserId()).getBody();
		
		helper.setTo(user.getEmail());
		helper.setSubject("Eshop Platform - Thank you for your order");
		Context context = new Context();
        context.setVariable("username", user.getUsername());
        context.setVariable("orderId", order.getId());
        String body = templateEngine.process("payment-confirmation-template", context);
		helper.setText(body, true);
		mailSender.send(message);
	}
	
	@Retry(fallbackMethod = "servicesDownFallback", name = "")
	public void sendOrderProcessingEmail(Long orderId) throws MessagingException {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		OrderDTO order = apiClient.getOrder(orderId).getBody();
		UserDTO user = apiClient.getUser(order.getUserId()).getBody();
		
		helper.setTo(user.getEmail());
		helper.setSubject("Eshop Platform - Information about state of your order");
		Context context = new Context();
        context.setVariable("username", user.getUsername());
        context.setVariable("orderId", order.getId());
        String body = templateEngine.process("order-processing-template", context);
		helper.setText(body, true);
		mailSender.send(message);
	}
	
	@Retry(fallbackMethod = "servicesDownFallback", name = "")
	public void sendOrderCancellationEmail(Long orderId) throws MessagingException {
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true);
		OrderDTO order = apiClient.getOrder(orderId).getBody();
		UserDTO user = apiClient.getUser(order.getUserId()).getBody();
		
		helper.setTo(user.getEmail());
		helper.setSubject("Eshop Platform - Order cancellation");
		Context context = new Context();
        context.setVariable("username", user.getUsername());
        context.setVariable("orderId", order.getId());
        String body = templateEngine.process("order-cancellation-template", context);
		helper.setText(body, true);
		mailSender.send(message);
	}
	
	private void servicesDownFallback(Exception e) {
        throw new BusinessException("Services are down");
    }
	
	private String generateVerificationToken(UserDTO user) {
		Algorithm algorithm = Algorithm.HMAC256(environment.getProperty("verification.secret.key").getBytes());
		String subject = user.getUsername();
		if(user.getRole().equals(Role.ADMIN) && !user.getActive()) {
			subject = user.getEmail();
		}
	    String verificationToken = JWT.create()
	    		.withSubject(subject)
	    		.withExpiresAt(new Date(System.currentTimeMillis() + 168 * 60 * 60 * 1000))
	            .sign(algorithm);
	    return verificationToken;
	}
}
