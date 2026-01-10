package com.example.OrderService.Repository;



import com.example.OrderService.Domain.ResDTO.ItemResponse;
import com.example.OrderService.Domain.ResDTO.RestResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "menu-service",url = "http://localhost:8083")
public interface MenuClient {

    @PostMapping("/api/v1/items/internal/batch-check")
    RestResponse<List<ItemResponse>> getProductsByIds(
            @RequestBody List<Integer> ids
    );

}