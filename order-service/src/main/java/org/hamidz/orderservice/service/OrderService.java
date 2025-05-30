package org.hamidz.orderservice.service;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hamidz.orderservice.dto.InventoryResponse;
import org.hamidz.orderservice.dto.OrderLineItemsDto;
import org.hamidz.orderservice.dto.OrderRequest;
import org.hamidz.orderservice.entity.Order;
import org.hamidz.orderservice.entity.OrderLineItems;
import org.hamidz.orderservice.event.OrderPlacedEvent;
import org.hamidz.orderservice.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;
    private final Tracer tracer;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    public OrderService(OrderRepository orderRepository,
                        WebClient.Builder webClientBuilder,
                        Tracer tracer,
                        KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate) {
        this.orderRepository = orderRepository;
        this.webClientBuilder = webClientBuilder
                .baseUrl("http://inventory-service")
                .build().mutate();
        this.tracer = tracer;
        this.kafkaTemplate = kafkaTemplate;
    }

    public String placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapToDto)
                .toList();

        order.setOrderLineItemsList(orderLineItems);

        List<String> skuCodes = order.getOrderLineItemsList()
                .stream()
                .map(OrderLineItems::getSkuCode)
                .toList();

        Span inventoryServiceLookup = tracer.nextSpan().name("InventoryServiceLookup");
        try (Tracer.SpanInScope spanInScope = tracer.withSpan(
                inventoryServiceLookup.start())){
            // call inventory service, if product is in stock
            try {
                // In placeOrder():
                log.info("Calling Inventory Service with SKUs: {}", skuCodes);
                InventoryResponse[] inventoryResponseArray = webClientBuilder.build().get()
                        .uri("http://inventory-service/api/inventory",
                                uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                        .retrieve()
                        .bodyToMono(InventoryResponse[].class)
                        .block();

                boolean allProductsInStock = Arrays.stream(inventoryResponseArray)
                        .allMatch(InventoryResponse::isInStock);

                if(allProductsInStock) {
                    orderRepository.save(order);
                    kafkaTemplate.send("notificationTopic", new OrderPlacedEvent("Order placed for order number: " + order.getOrderNumber()));
                    return "Order placed successfully! " + orderRequest.getOrderLineItemsDtoList().size() + " items ordered.";
                } else {
                    throw new IllegalArgumentException("Product is not in stock, please try again later");
                }
            } catch (WebClientResponseException ex) {
                // Log status and response body for debugging
                System.err.println("Error: " + ex.getStatusCode() + " - " + ex.getResponseBodyAsString());
                throw ex;
            }
        } finally {
            inventoryServiceLookup.end();
        }
    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItems;
    }
}
