package org.hamidz.orderservice.controller;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import org.hamidz.orderservice.dto.OrderRequest;
import org.hamidz.orderservice.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping(path = "/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackMethod")
    @TimeLimiter(name = "inventory")
    @Retry(name = "inventory")
    public CompletableFuture<String> placeOrder(
            @RequestBody OrderRequest orderRequest) {
        orderService.placeOrder(orderRequest);
//        return "Order placed successfully! " + orderRequest.getOrderLineItemsDtoList().size() + " items ordered.";
        return CompletableFuture.supplyAsync(
                () -> orderService.placeOrder(orderRequest));
    }

    public CompletableFuture<String> fallbackMethod(
            OrderRequest orderRequest,
            RuntimeException runtimeException) {
        return CompletableFuture.supplyAsync(
                () -> "OOPS! Something went wrong, " + "please order after some time!");
    }
}
