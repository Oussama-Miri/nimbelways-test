package com.nimbleways.springboilerplate.controllers;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.enumeration.ProductType;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.implementations.NotificationService;
import com.nimbleways.springboilerplate.utils.Annotations.SetupDatabase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.nimbleways.springboilerplate.enumeration.ProductType.*;
import static org.junit.Assert.assertEquals;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SetupDatabase
@SpringBootTest
@AutoConfigureMockMvc
public class ProductControllerIntegrationTests {
        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private NotificationService notificationService;

        @Autowired
        private OrderRepository orderRepository;

        @Autowired
        private ProductRepository productRepository;

        @Test
        public void processOrderShouldReturn() throws Exception {
                List<Product> allProducts = createProducts();
                Set<Product> orderItems = new HashSet<>(allProducts);
                Order order = createOrder(orderItems);
                productRepository.saveAll(allProducts);
                order = orderRepository.save(order);
                mockMvc.perform(post("/orders/{orderId}/processOrder", order.getId())
                        .contentType("application/json"))
                        .andExpect(status().isOk());
                Order resultOrder = orderRepository.findById(order.getId()).get();
                assertEquals(resultOrder.getId(), order.getId());
        }

        private static Order createOrder(Set<Product> products) {
                Order order = new Order();
                order.setItems(products);
                return order;
        }

        private static List<Product> createProducts() {
                List<Product> products = new ArrayList<>();
                products.add(new Product(null, 15, 30, NORMAL, "USB Cable", null, null, null));
                products.add(new Product(null, 10, 0, NORMAL, "USB Dongle", null, null, null));
                products.add(new Product(null, 15, 30, EXPIRABLE, "Butter", LocalDate.now().plusDays(26), null, null));
                products.add(new Product(null, 90, 6, EXPIRABLE, "Milk", LocalDate.now().minusDays(2), null, null));
                products.add(new Product(null, 15, 30, SEASONAL, "Watermelon", null, LocalDate.now().minusDays(2),
                        LocalDate.now().plusDays(58)));
                products.add(new Product(null, 15, 30, SEASONAL, "Grapes", null, LocalDate.now().plusDays(180),
                        LocalDate.now().plusDays(240)));
                products.add(new Product(null, 15, 30, FLASHSALE, "Flash Sale Product", null, LocalDate.now(), LocalDate.now().plusDays(7)));
                return products;
        }
}
