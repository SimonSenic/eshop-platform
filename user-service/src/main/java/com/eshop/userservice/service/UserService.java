package com.eshop.userservice.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.eshop.userservice.dto.UpdateUserDTO;
import com.eshop.userservice.dto.UserDTO;
import com.eshop.userservice.entity.User;
import com.eshop.userservice.exception.BusinessException;
import com.eshop.userservice.exception.NotFoundException;
import com.eshop.userservice.mapper.UserMapper;
import com.eshop.userservice.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class UserService implements UserDetailsService{
	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final PasswordEncoder passwordEncoder;
	private final UserAuthentication userAuthentication;
	private final Environment environment;
	
	public UserDTO login(UserDTO userDTO) {
		User user = userRepository.findByUsername(userDTO.getUsername())
				.orElseThrow(() -> new NotFoundException("User not found"));
		return userMapper.toDTO(user);
	}
	
	public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Authentication auth = userAuthentication.getAuthentication();
		
		if(auth.isAuthenticated()) {
			User user = userRepository.findByUsername(auth.getName())
					.orElseThrow(() -> new NotFoundException("User not found"));
			Algorithm algorithm = Algorithm.HMAC256(environment.getProperty("secret.key").getBytes());
		    String access_token = JWT.create()
		    		.withSubject(user.getUsername())
		    		.withExpiresAt(new Date(System.currentTimeMillis() + 15 * 60 * 1000))
		            .withIssuer(request.getRequestURL().toString())
		            .withClaim("roles", Arrays.asList(user.getRole().name()))
		            .sign(algorithm);
		    
		    Map<String, String> map = new HashMap<>();
		    map.put("access_token", access_token);
		    map.put("refresh_token", request.getHeader("Authorization").substring(7));
		    map.put("role", user.getRole().name());
		    response.setContentType("application/json");
		    new ObjectMapper().writeValue(response.getOutputStream(), map);
		}
	}
	
	public UserDTO updateUser(UpdateUserDTO updateUserDTO) {
		User user = userRepository.findByUsername(userAuthentication.getAuthentication().getName())
				.orElseThrow(() -> new NotFoundException("User not found"));
		if(!passwordEncoder.matches(updateUserDTO.getPassword(), user.getPassword())) { 
			throw new BusinessException("Invalid password");
		}else if(updateUserDTO.getPassword().equals(updateUserDTO.getNewPassword())) {
			throw new BusinessException("New password must not be the same");
		}
		
		user = userMapper.updateUser(user, updateUserDTO);
		userRepository.save(user);
		log.info("User updated successfully (userId: {})", user.getId());
		return userMapper.toDTO(user);
	}
	
	public UserDTO getUserProfile() {
		User user = userRepository.findByUsername(userAuthentication.getAuthentication().getName())
				.orElseThrow(() -> new NotFoundException("User not found"));
		return userMapper.toDTO(user);
	}
	
	@Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if(!userRepository.findByUsername(username).filter(temp -> temp.getActive()).isPresent()) {
           throw  new UsernameNotFoundException("User not found");
        }
        
        User user = userRepository.findByUsername(username).get();
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(user.getRole().toString()));
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(), user.getPassword(), authorities);
    }
	
}
