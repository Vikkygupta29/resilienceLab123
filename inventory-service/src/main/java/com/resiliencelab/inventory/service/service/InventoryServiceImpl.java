package com.resiliencelab.inventory.service.service;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import com.resiliencelab.inventory.service.dto.InventoryResponse;
import com.resiliencelab.inventory.service.dto.ReserveInventoryRequest;
import com.resiliencelab.inventory.service.entity.Inventory;
import com.resiliencelab.inventory.service.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;


@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService{

    private final InventoryRepository inventoryRepository;


    @Override
    @Cacheable(value = "inventory", key = "#productId")
    public InventoryResponse getInventoryById(String productId) {
        Inventory inventory = inventoryRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND ,"product not found"));

        return InventoryResponse.from(inventory);
    }

    @CacheEvict(value = "inventory", key = "#productId")
    @Transactional
    public InventoryResponse reserveInventory(String productId, ReserveInventoryRequest request){

        Inventory inventory = inventoryRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND ,"product not found"));

        inventory.reserve(request.quantity());

        return InventoryResponse.from(inventory);


    }
}
