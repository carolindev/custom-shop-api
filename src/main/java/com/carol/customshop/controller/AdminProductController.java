package com.carol.customshop.controller;

import com.carol.customshop.api.AdminProductApi;
import com.carol.customshop.dto.*;
import com.carol.customshop.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
public class AdminProductController implements AdminProductApi {
    ProductService productService;
    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }
    @Override
    public ResponseEntity<ProductCreationResponse> createProduct(
            String name,
            String sku,
            String description,
            Float price,
            UUID productTypeId,
            MultipartFile mainPicture,
            List<MultipartFile> imageGallery,
            ProductAttributeOverrides overrides,
            NotAllowedCombinationsOverrides nACombinationsOverrides,
            List<List<NotAllowedCombinationItem>> productNotAllowedCombinations
    ) {
        ProductCreationResponse response = productService.createProduct(
                name, sku, description, price, productTypeId,
                mainPicture, imageGallery, overrides, nACombinationsOverrides, productNotAllowedCombinations
        );

        return ResponseEntity.status(201).body(response);
    }

    @Override
    public ResponseEntity<ProductListResponse> getAdminProductList(Integer page, Integer size) {
        ProductListResponse response = productService.getProductList(page, size);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ProductDetailsResponse> getProductDetails(UUID productId) {
        ProductDetailsResponse productDetails = productService.getProductDetails(productId);
        return ResponseEntity.ok(productDetails);
    }
}
