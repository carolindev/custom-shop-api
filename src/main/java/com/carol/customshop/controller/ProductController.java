package com.carol.customshop.controller;

import com.carol.customshop.api.ProductApi;
import com.carol.customshop.dto.AvailableAttributeOptionsResponse;
import com.carol.customshop.dto.ProductDetailsCustomerResponse;
import com.carol.customshop.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
public class ProductController implements ProductApi {

    ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public ResponseEntity<AvailableAttributeOptionsResponse> getAvailableOptions(
            UUID productId,
            Long requestedAttributeId,
            String selectedOptionIds
    ) {
        // Parse selected option IDs from CSV format (e.g., "2,6,9" → List<Long>)
        List<Long> selectedOptions = Arrays.stream(selectedOptionIds.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toList());

        // Call service to get available options
        AvailableAttributeOptionsResponse response = productService.getAvailableOptionsBySelection(
                productId, requestedAttributeId, selectedOptions);

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ProductDetailsCustomerResponse> getProductDetailsForCustomer(UUID productId) {
        ProductDetailsCustomerResponse response = productService.getProductDetailsForCustomer(productId);
        return ResponseEntity.ok(response);
    }
}
