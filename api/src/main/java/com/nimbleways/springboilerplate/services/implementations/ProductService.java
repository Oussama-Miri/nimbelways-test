package com.nimbleways.springboilerplate.services.implementations;

import java.time.LocalDate;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.enumeration.ProductType;
import com.nimbleways.springboilerplate.exception.OrderNotFoundException;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

    @Autowired
    public ProductService(ProductRepository productRepository, OrderRepository orderRepository, NotificationService notificationService) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.notificationService = notificationService;
    }

    public Order processOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order with ID " + orderId + " not found"));

        for (Product product : order.getItems()) {
            ProductType productType = product.getType();

            if (ProductType.FLASHSALE.equals(productType)) {
                handleFlashSaleProduct(product);
            } else if (ProductType.NORMAL.equals(productType)) {
                handleNormalProduct(product);
            } else if (ProductType.SEASONAL.equals(productType)) {
                handleSeasonalProduct(product);
            } else if (ProductType.EXPIRABLE.equals(productType)) {
                handleExpirableProduct(product);
            }
        }

        return orderRepository.save(order);
    }

    private void handleSeasonalProduct(Product product) {
        if (product.getUnitsAvailable() > 0 && LocalDate.now().isAfter(product.getSeasonStartDate())
                && LocalDate.now().isBefore(product.getSeasonEndDate())) {
            product.setUnitsAvailable(product.getUnitsAvailable() - 1);
        } else {
            notificationService.sendOutOfStockNotification(product.getName());
        }
        productRepository.save(product);
    }

    private void handleFlashSaleProduct(Product product) {
        if (product.getUnitsAvailable() > 0 && isFlashSaleOngoing(product)) {
            product.setUnitsAvailable(product.getUnitsAvailable() - 1);
        } else {
            product.setUnitsAvailable(0);
        }
        productRepository.save(product);
    }

    private boolean isFlashSaleOngoing(Product product) {
        LocalDate now = LocalDate.now();
        LocalDate flashSaleStartDate = product.getSeasonStartDate();
        LocalDate flashSaleEndDate = product.getSeasonEndDate();
        return now.isAfter(flashSaleStartDate) && now.isBefore(flashSaleEndDate);
    }

    private void handleNormalProduct(Product product) {
        if (product.getUnitsAvailable() > 0) {
            product.setUnitsAvailable(product.getUnitsAvailable() - 1);
            productRepository.save(product);
        } else {
            int leadTime = product.getLeadTime();
            if (leadTime > 0) {
                notifyDelay(leadTime, product);
            }
        }
    }

    private void handleExpirableProduct(Product product) {
        if (product.getUnitsAvailable() > 0 && product.getExpiryDate().isAfter(LocalDate.now())) {
            product.setUnitsAvailable(product.getUnitsAvailable() - 1);
            productRepository.save(product);
        } else {
            notificationService.sendExpirationNotification(product.getName(), product.getExpiryDate());
            product.setUnitsAvailable(0);
            productRepository.save(product);
        }
    }

    public void notifyDelay(int leadTime, Product product) {
        product.setLeadTime(leadTime);
        productRepository.save(product);
        notificationService.sendDelayNotification(leadTime, product.getName());
    }

    // We can implement other handling methods for different product types in the future...
}
