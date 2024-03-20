package com.eshop.orderservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import com.eshop.orderservice.dto.ItemDTO;
import com.eshop.orderservice.dto.OrderDTO;
import com.eshop.orderservice.dto.ProductDTO;
import com.eshop.orderservice.dto.Role;
import com.eshop.orderservice.dto.UserDTO;
import com.eshop.orderservice.entity.Item;
import com.eshop.orderservice.entity.Order;
import com.eshop.orderservice.entity.State;
import com.eshop.orderservice.mapper.OrderMapper;
import com.eshop.orderservice.repository.ItemRepository;
import com.eshop.orderservice.repository.OrderRepository;

@SpringBootTest
class OrderServiceTests {
	
	@InjectMocks
	private OrderService orderService;
	
	@Mock
	private OrderRepository orderRepository;
	
	@Mock
	private ItemRepository itemRepository;
	
	@Mock
	private OrderMapper orderMapper;
	
	@Mock
	private ApiClient apiClient;
	
	private final String PRODUCT_NAME = "Product 1";
	private final BigDecimal PRODUCT_PRICE = BigDecimal.valueOf(2.60);
	private final int PRODUCT_AVAILABILITY = 3;	
	private final BigDecimal PRODUCT2_PRICE = BigDecimal.valueOf(1.80);
	
	private final String USERNAME = "User123";
	
	@Test
	void testSuccessfullyGetOrders() {
		Order order = new Order(1L, new ArrayList<Item>(), State.DRAFT);
		Item item = new Item(order, 2L);
		item.setAmount(1);
		item.setPrice(PRODUCT_PRICE);
		order.getCart().add(item);
		order.setTotalPrice(item.getPrice());
		
		Order order2 = new Order(1L, new ArrayList<Item>(), State.ORDER);
		Item item2 = new Item(order2, 3L);
		item2.setAmount(2);
		item2.setPrice(PRODUCT2_PRICE.multiply(BigDecimal.valueOf(2)));
		order2.getCart().add(item2);
		order2.setTotalPrice(item2.getPrice());
		
		ItemDTO itemDTO = ItemDTO.builder()
				.id(1L)
				.productId(2L)
				.amount(1)
				.price(PRODUCT_PRICE).build();
		
		OrderDTO orderDTO = OrderDTO.builder()
				.id(1L)
				.userId(1L)
				.cart(List.of(itemDTO))
				.totalPrice(itemDTO.getPrice())
				.state(State.DRAFT).build();
		
		ItemDTO item2DTO = ItemDTO.builder()
				.id(2L)
				.productId(3L)
				.amount(2)
				.price(PRODUCT2_PRICE.multiply(BigDecimal.valueOf(2))).build();
		
		OrderDTO order2DTO = OrderDTO.builder()
				.id(2L)
				.userId(2L)
				.cart(List.of(item2DTO))
				.totalPrice(item2DTO.getPrice())
				.state(State.ORDER).build();
		
		Page<Order> orders = new PageImpl<>(List.of(order, order2));
		Page<OrderDTO> ordersDTO = new PageImpl<>(List.of(orderDTO, order2DTO));
		
		when(orderRepository.findAll(any(Pageable.class))).thenReturn(orders);
		when(orderMapper.toDTOs(Mockito.<Page<Order>>any())).thenReturn(ordersDTO);
		
		Page<OrderDTO> result = orderService.getOrders(PageRequest.of(0, 20));
		
		assertThat(result).isNotNull().isNotEmpty()
		.extracting(OrderDTO::getId, OrderDTO::getUserId, OrderDTO::getTotalPrice, OrderDTO::getState)
		.containsExactly(
				tuple(1L, 1L, PRODUCT_PRICE, State.DRAFT),
				tuple(2L, 2L, PRODUCT2_PRICE.multiply(BigDecimal.valueOf(2)), State.ORDER));
		
		assertThat(result.getContent().get(0).getCart()).isNotNull().isNotEmpty()
		.extracting(ItemDTO::getId, ItemDTO::getProductId, ItemDTO::getAmount, ItemDTO::getPrice)
		.containsExactly(
				tuple(1L, 2L, 1, PRODUCT_PRICE));
		
		assertThat(result.getContent().get(1).getCart()).isNotNull().isNotEmpty()
		.extracting(ItemDTO::getId, ItemDTO::getProductId, ItemDTO::getAmount, ItemDTO::getPrice)
		.containsExactly(
				tuple(2L, 3L, 2, PRODUCT2_PRICE.multiply(BigDecimal.valueOf(2))));
	}
	
	@Test
	void testSuccessfullyGetOrder() {
		Order order = new Order(1L, new ArrayList<Item>(), State.COMPLETED);
		Item item = new Item(order, 2L);
		item.setAmount(5);
		item.setPrice(PRODUCT_PRICE.multiply(BigDecimal.valueOf(5)));
		
		Item item2 = new Item(order, 3L);
		item2.setAmount(3);
		item2.setPrice(PRODUCT_PRICE.multiply(BigDecimal.valueOf(3)));
		order.getCart().addAll(List.of(item, item2));
		order.setTotalPrice(item.getPrice().add(item2.getPrice()));
		
		ItemDTO itemDTO = ItemDTO.builder()
				.id(1L)
				.productId(2L)
				.amount(5)
				.price(PRODUCT_PRICE.multiply(BigDecimal.valueOf(5))).build();
		
		ItemDTO item2DTO = ItemDTO.builder()
				.id(2L)
				.productId(3L)
				.amount(3)
				.price(PRODUCT2_PRICE.multiply(BigDecimal.valueOf(3))).build();
		
		OrderDTO orderDTO = OrderDTO.builder()
				.id(1L)
				.userId(1L)
				.cart(List.of(itemDTO, item2DTO))
				.totalPrice(itemDTO.getPrice().add(item2DTO.getPrice()))
				.state(State.COMPLETED).build();
		
		when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
		when(orderMapper.toDTO(any(Order.class))).thenReturn(orderDTO);
		
		OrderDTO result = orderService.getOrder(1L);
		
		assertThat(result).isNotNull()
		.extracting(OrderDTO::getId, OrderDTO::getUserId, OrderDTO::getTotalPrice, OrderDTO::getState)
		.containsExactly(1L, 1L, PRODUCT_PRICE.multiply(BigDecimal.valueOf(5)).add(PRODUCT2_PRICE.multiply(BigDecimal.valueOf(3))), State.COMPLETED);
		
		assertThat(result.getCart()).isNotNull().isNotEmpty()
		.extracting(ItemDTO::getId, ItemDTO::getProductId, ItemDTO::getAmount, ItemDTO::getPrice)
		.containsExactly(
				tuple(1L, 2L, 5, PRODUCT_PRICE.multiply(BigDecimal.valueOf(5))),
				tuple(2L, 3L, 3, PRODUCT2_PRICE.multiply(BigDecimal.valueOf(3))));
	}
	
	@Test
	void testSuccessfullyGetMyOrders() {
		Order order = new Order(1L, new ArrayList<Item>(), State.CANCELLED);
		Item item = new Item(order, 2L);
		item.setPrice(PRODUCT_PRICE);
		item.setAmount(1);
		order.getCart().add(item);
		order.setTotalPrice(PRODUCT_PRICE);
		
		Order order2 = new Order(1L, new ArrayList<Item>(), State.CONFIRMED);
		Item item2 = new Item(order2, 3L);
		item2.setPrice(PRODUCT2_PRICE);
		item2.setAmount(3);
		order2.getCart().add(item2);
		order2.setTotalPrice(PRODUCT2_PRICE);
		
		UserDTO userDTO = UserDTO.builder()
				.id(1L)
				.username(USERNAME)
				.role(Role.CUSTOMER)
				.active(true).build();
		
		ItemDTO itemDTO = ItemDTO.builder()
				.id(1L)
				.productId(2L)
				.amount(1)
				.price(PRODUCT_PRICE).build();
		
		OrderDTO orderDTO = OrderDTO.builder()
				.id(1L)
				.userId(1L)
				.cart(List.of(itemDTO))
				.totalPrice(itemDTO.getPrice())
				.state(State.CANCELLED).build();
		
		ItemDTO item2DTO = ItemDTO.builder()
				.id(2L)
				.productId(3L)
				.amount(3)
				.price(PRODUCT2_PRICE.multiply(BigDecimal.valueOf(3))).build();
		
		OrderDTO order2DTO = OrderDTO.builder()
				.id(2L)
				.userId(1L)
				.cart(List.of(item2DTO))
				.totalPrice(item2DTO.getPrice())
				.state(State.CONFIRMED).build();
		
		when(apiClient.getUserProfile()).thenReturn(ResponseEntity.of(Optional.of(userDTO)));
		when(orderRepository.findByUserId(anyLong())).thenReturn(List.of(order, order2));
		when(orderMapper.toDTOs(Mockito.<List<Order>>any())).thenReturn(List.of(orderDTO, order2DTO));
		
		List<OrderDTO> result = orderService.getMyOrders();
		
		assertThat(result).isNotNull().isNotEmpty()
		.extracting(OrderDTO::getId, OrderDTO::getUserId, OrderDTO::getTotalPrice, OrderDTO::getState)
		.containsExactly(
				tuple(1L, 1L, PRODUCT_PRICE, State.CANCELLED),
				tuple(2L, 1L, PRODUCT2_PRICE.multiply(BigDecimal.valueOf(3)), State.CONFIRMED));
		
		assertThat(result.get(0).getCart()).isNotNull().isNotEmpty()
		.extracting(ItemDTO::getId, ItemDTO::getProductId, ItemDTO::getAmount, ItemDTO::getPrice)
		.containsExactly(
				tuple(1L, 2L, 1, PRODUCT_PRICE));
		
		assertThat(result.get(1).getCart()).isNotNull().isNotEmpty()
		.extracting(ItemDTO::getId, ItemDTO::getProductId, ItemDTO::getAmount, ItemDTO::getPrice)
		.containsExactly(
				tuple(2L, 3L, 3, PRODUCT2_PRICE.multiply(BigDecimal.valueOf(3))));
	}

	@Test
	void testSuccessfullyUpdateOrder() {	
		Order order = new Order(1L, new ArrayList<Item>(), State.DRAFT);
		Item item = new Item(order, 2L);
		item.setAmount(1);
		item.setPrice(PRODUCT_PRICE);
		order.getCart().add(item);
		order.setTotalPrice(item.getPrice());
		
		ProductDTO productDTO = ProductDTO.builder()
				.id(2L)
				.name(PRODUCT_NAME)
				.price(PRODUCT_PRICE)
				.availability(PRODUCT_AVAILABILITY).build();
		
		ItemDTO itemDTO = ItemDTO.builder()
				.id(1L)
				.productId(2L)
				.amount(3)
				.price(PRODUCT_PRICE.multiply(BigDecimal.valueOf(3))).build();
		
		OrderDTO orderDTO = OrderDTO.builder()
				.id(1L)
				.userId(1L)
				.cart(List.of(itemDTO))
				.totalPrice(itemDTO.getPrice())
				.state(State.DRAFT).build();
		
		when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
		when(itemRepository.findByOrderIdAndProductId(anyLong(), anyLong())).thenReturn(Optional.of(item));
		when(apiClient.getProduct(anyLong())).thenReturn(ResponseEntity.of(Optional.of(productDTO)));
		when(orderMapper.toDTO(any(Order.class))).thenReturn(orderDTO);
		
		OrderDTO result = orderService.updateOrder(1L, 1L, 3);
		
		verify(orderRepository).save(any(Order.class));
		
		assertThat(result).isNotNull()
		.extracting(OrderDTO::getId, OrderDTO::getUserId, OrderDTO::getTotalPrice, OrderDTO::getState)
		.containsExactly(1L, 1L, PRODUCT_PRICE.multiply(BigDecimal.valueOf(3)), State.DRAFT);
		
		assertThat(result.getCart()).isNotNull().isNotEmpty()
		.extracting(ItemDTO::getId, ItemDTO::getProductId, ItemDTO::getAmount, ItemDTO::getPrice)
		.containsExactly(
				tuple(1L, 2L, 3, PRODUCT_PRICE.multiply(BigDecimal.valueOf(3))));
	}
	
	@Test
	void testSuccessfullyCancelOrder() {
		Order order = new Order(1L, new ArrayList<Item>(), State.CONFIRMED);
		Item item = new Item(order, 2L);
		item.setAmount(5);
		item.setPrice(PRODUCT_PRICE.multiply(BigDecimal.valueOf(5)));
		
		Item item2 = new Item(order, 3L);
		item2.setAmount(3);
		item2.setPrice(PRODUCT2_PRICE.multiply(BigDecimal.valueOf(3)));
		order.getCart().addAll(List.of(item, item2));
		order.setTotalPrice(item.getPrice().add(item2.getPrice()));
		
		ItemDTO itemDTO = ItemDTO.builder()
				.id(1L)
				.productId(2L)
				.amount(5)
				.price(PRODUCT_PRICE.multiply(BigDecimal.valueOf(5))).build();
		
		ItemDTO item2DTO = ItemDTO.builder()
				.id(2L)
				.productId(3L)
				.amount(3)
				.price(PRODUCT2_PRICE.multiply(BigDecimal.valueOf(3))).build();
		
		OrderDTO orderDTO = OrderDTO.builder()
				.id(1L)
				.userId(1L)
				.cart(List.of(itemDTO, item2DTO))
				.totalPrice(itemDTO.getPrice().add(item2DTO.getPrice()))
				.state(State.CANCELLED).build();
		
		when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
		when(orderMapper.toDTO(any(Order.class))).thenReturn(orderDTO);
		
		OrderDTO result = orderService.cancelOrder(1L);
		
		verify(apiClient, times(2)).updateAvailability(anyLong(), anyInt());
		verify(orderRepository).save(any(Order.class));
		
		assertThat(result).isNotNull()
		.extracting(OrderDTO::getId, OrderDTO::getUserId, OrderDTO::getTotalPrice, OrderDTO::getState)
		.containsExactly(1L, 1L, PRODUCT_PRICE.multiply(BigDecimal.valueOf(5)).add(PRODUCT2_PRICE.multiply(BigDecimal.valueOf(3))), State.CANCELLED);
	}
	
	@Test
	void testSuccessfullyProcessOrder() {
		Order order = new Order(1L, new ArrayList<Item>(), State.ORDER);
		Item item = new Item(order, 2L);
		item.setAmount(5);
		item.setPrice(PRODUCT_PRICE.multiply(BigDecimal.valueOf(5)));
		
		Item item2 = new Item(order, 3L);
		item2.setAmount(3);
		item2.setPrice(PRODUCT2_PRICE.multiply(BigDecimal.valueOf(3)));
		order.getCart().addAll(List.of(item, item2));
		order.setTotalPrice(item.getPrice().add(item2.getPrice()));
		
		ItemDTO itemDTO = ItemDTO.builder()
				.id(1L)
				.productId(2L)
				.amount(5)
				.price(PRODUCT_PRICE.multiply(BigDecimal.valueOf(5))).build();
		
		ItemDTO item2DTO = ItemDTO.builder()
				.id(2L)
				.productId(3L)
				.amount(3)
				.price(PRODUCT2_PRICE.multiply(BigDecimal.valueOf(3))).build();
		
		OrderDTO orderDTO = OrderDTO.builder()
				.id(1L)
				.userId(1L)
				.cart(List.of(itemDTO, item2DTO))
				.totalPrice(itemDTO.getPrice().add(item2DTO.getPrice()))
				.state(State.CONFIRMED).build();
		
		when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
		when(orderMapper.toDTO(any(Order.class))).thenReturn(orderDTO);
		
		OrderDTO result = orderService.processOrder(1L, State.CONFIRMED);
		
		verify(orderRepository).save(any(Order.class));
		
		assertThat(result).isNotNull()
		.extracting(OrderDTO::getId, OrderDTO::getUserId, OrderDTO::getTotalPrice, OrderDTO::getState)
		.containsExactly(1L, 1L, PRODUCT_PRICE.multiply(BigDecimal.valueOf(5)).add(PRODUCT2_PRICE.multiply(BigDecimal.valueOf(3))), State.CONFIRMED);
	}
	
	@Test
	void testSuccessfullyCreateOrder() {
		Order order = new Order(1L, new ArrayList<Item>(), State.DRAFT);
		Item item = new Item(order, 2L);
		item.setAmount(1);
		item.setPrice(PRODUCT_PRICE);
		order.getCart().add(item);
		order.setTotalPrice(item.getPrice());
		
		UserDTO userDTO = UserDTO.builder()
				.id(1L)
				.username(USERNAME)
				.role(Role.CUSTOMER)
				.active(true).build();
		
		ProductDTO productDTO = ProductDTO.builder()
				.id(2L)
				.name(PRODUCT_NAME)
				.price(PRODUCT_PRICE)
				.availability(PRODUCT_AVAILABILITY).build();
		
		when(apiClient.getUserProfile(anyString())).thenReturn(ResponseEntity.of(Optional.of(userDTO)));
		when(orderRepository.findByUserIdAndState(anyLong(), any(State.class))).thenReturn(Optional.of(order));
		when(itemRepository.findByOrderIdAndProductId(anyLong(), anyLong())).thenReturn(Optional.of(item));
		
		orderService.createOrder(productDTO, 2, "token");
		
		verify(orderRepository).save(any(Order.class));
	}
	
	@Test
	void testSuccessfullyCompletePayment() {
		Order order = new Order(1L, new ArrayList<Item>(), State.DRAFT);
		Item item = new Item(order, 2L);
		item.setAmount(5);
		item.setPrice(PRODUCT_PRICE.multiply(BigDecimal.valueOf(5)));
		
		Item item2 = new Item(order, 3L);
		item2.setAmount(3);
		item2.setPrice(PRODUCT2_PRICE.multiply(BigDecimal.valueOf(3)));
		order.getCart().addAll(List.of(item, item2));
		order.setTotalPrice(item.getPrice().add(item2.getPrice()));
		
		when(orderRepository.findById(anyLong())).thenReturn(Optional.of(order));
		
		orderService.completePayment(1L);
		
		verify(orderRepository).save(any(Order.class));
		verify(apiClient, times(2)).updateAvailability(anyLong(), anyInt());
	}

}
