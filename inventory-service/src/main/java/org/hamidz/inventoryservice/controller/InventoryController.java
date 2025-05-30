package org.hamidz.inventoryservice.controller;

import lombok.RequiredArgsConstructor;
import org.hamidz.inventoryservice.dto.InventoryResponse;
import org.hamidz.inventoryservice.service.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<InventoryResponse> isInStock(
            @RequestParam List<String> skuCode) {
        return inventoryService.isInStock(skuCode);
    }
}
