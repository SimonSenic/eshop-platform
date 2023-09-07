package com.eshop.orderservice.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.eshop.orderservice.dto.OrderDTO;
import com.eshop.orderservice.dto.ProductDTO;
import com.eshop.orderservice.dto.UserDTO;
import com.eshop.orderservice.entity.Item;
import com.eshop.orderservice.entity.Order;
import com.eshop.orderservice.entity.State;
import com.eshop.orderservice.exception.BusinessException;
import com.eshop.orderservice.exception.NotFoundException;
import com.eshop.orderservice.mapper.OrderMapper;
import com.eshop.orderservice.repository.ItemRepository;
import com.eshop.orderservice.repository.OrderRepository;

import io.github.resilience4j.retry.annotation.Retry;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class OrderService {
	private final OrderRepository orderRepository;
	private final ItemRepository itemRepository;
	private final OrderMapper orderMapper;
	private final ApiClient apiClient;
	
	public Page<OrderDTO> getOrders(Pageable pageable){
		return orderMapper.toDTOs(orderRepository.findAll(pageable));
	}

	public OrderDTO getOrder(Long id) {
		Order order = orderRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Order not found"));
		return orderMapper.toDTO(order);
	}
	
	public List<OrderDTO> getMyOrders(){
		UserDTO user = apiClient.getUserProfile().getBody();
		List<Order> orders = orderRepository.findByUserId(user.getId());
		return orderMapper.toDTOs(orders);
	}
	
	@Transactional
	public OrderDTO updateOrder(Long id, Long productId, Integer amount) {
		Order order = orderRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Order not found"));
		if(!order.getState().equals(State.DRAFT)) {
			throw new BusinessException("Invalid order");	
		}
		
		if(productId != null) {
			Item item = itemRepository.findByOrderIdAndProductId(id, productId)
					.orElseThrow(() -> new NotFoundException("Item not found"));
			if(amount != null && amount != 0) {
				ProductDTO product = apiClient.getProduct(productId).getBody();
				item.setAmount(amount);
				item.setPrice(product.getPrice().multiply(BigDecimal.valueOf(amount)));
			}else {
				order.getCart().remove(item);
				itemRepository.delete(item);
			}
			order.setTotalPrice(BigDecimal.valueOf(0));
			order.getCart().forEach(temp -> order.setTotalPrice(order.getTotalPrice().add(temp.getPrice())));
		}else {
			itemRepository.deleteAllByOrderId(id);
			order.setTotalPrice(BigDecimal.valueOf(0));
		}
		
		orderRepository.save(order);
		log.info("Order updated successfully (orderId: {})", id);
		return orderMapper.toDTO(order);
	}
	
	@Transactional
	public OrderDTO cancelOrder(Long id) {
		Order order = orderRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Order not found"));
		if(!order.getState().equals(State.ORDER) && !order.getState().equals(State.CONFIRMED)) {
			throw new BusinessException("Invalid order state");
		}
		
		order.setState(State.CANCELLED);
		order.getCart().forEach(item -> apiClient.updateAvailability(item.getProductId(), item.getAmount()));
		orderRepository.save(order);
		log.info("Order cancelled successfully (orderId: {})", id);
		log.info("Send order cancellation email (orderId: {})", id);
		return orderMapper.toDTO(order);
	}
	
	public OrderDTO processOrder(Long id, State state) {
		Order order = orderRepository.findById(id)
				.orElseThrow(() -> new NotFoundException("Order not found"));
		if(order.getState().equals(State.DRAFT) || state.equals(State.DRAFT)) {
			throw new BusinessException("Invalid order state");
		}
		
		order.setState(state);
		orderRepository.save(order);
		log.info("Order state changed successfully (orderId: {})", id);
		if(state.equals(State.CONFIRMED)) {
			log.info("Send order processing email (orderId: {})", id);
		}else if(state.equals(State.CANCELLED)) {
			log.info("Send order cancellation email (orderId: {})", id);
		}
		return orderMapper.toDTO(order);
	}
	
	@Transactional
	@Retry(fallbackMethod = "userServiceDownFallback", name = "")
	public void createOrder(ProductDTO product, Integer amount, String token) {
		UserDTO user = apiClient.getUserProfile(token).getBody();
		List<SimpleGrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority(user.getRole().name()));
		Authentication auth = new UsernamePasswordAuthenticationToken(user.getUsername(), null, authorities);
		SecurityContextHolder.getContext().setAuthentication(auth);
		
		Order order = orderRepository.findByUserIdAndState(user.getId(), State.DRAFT)
				.orElse(new Order(user.getId(), new ArrayList<Item>(), State.DRAFT));
		Item item = itemRepository.findByOrderIdAndProductId(order.getId(), product.getId())
				.orElse(new Item(order, product.getId()));
		
		if(order.getCart().contains(item)) {
			item.setAmount(item.getAmount() + amount);
			item.setPrice(item.getPrice().add(product.getPrice().multiply(BigDecimal.valueOf(amount))));
		}else {
			item.setAmount(amount);
			item.setPrice(product.getPrice().multiply(BigDecimal.valueOf(amount)));
			order.getCart().add(item);
		}
		order.setTotalPrice(BigDecimal.valueOf(0));
		order.getCart().forEach(temp -> order.setTotalPrice(order.getTotalPrice().add(temp.getPrice())));
		orderRepository.save(order);
		log.info("Product added to cart successfully (productId: {})", product.getId());
	}
	
	@Transactional
	@Retry(fallbackMethod = "storageServiceDownFallback", name = "")
	public void completePayment(Long orderId) {
		Order order = orderRepository.findById(orderId)
				.orElseThrow(() -> new NotFoundException("Order not found"));
		if(!order.getState().equals(State.DRAFT)) {
			throw new BusinessException("Invalid order state");
		}
		
		order.setState(State.ORDER);
		order.getCart().forEach(item -> apiClient.updateAvailability(item.getProductId(), -item.getAmount()));
		orderRepository.save(order);
	}
	
	private void userServiceDownFallback(Exception exception) throws Exception {
		if(!(exception instanceof BusinessException)) {
			throw new BusinessException("User service is down");
		}else {
			throw exception;
		}
    }
	
	private void storageServiceDownFallback(Exception exception) throws Exception {
		if(!(exception instanceof BusinessException) && !(exception instanceof NotFoundException)) {
			throw new BusinessException("Storage service is down");
		}else {
			throw exception;
		}
    }
	
}
