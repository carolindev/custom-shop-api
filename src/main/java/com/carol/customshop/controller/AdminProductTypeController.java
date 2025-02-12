package com.carol.customshop.controller;

import com.carol.customshop.api.AdminProductTypesApi;
import com.carol.customshop.dto.*;
import com.carol.customshop.service.ProductTypeService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class AdminProductTypeController implements AdminProductTypesApi {

    private final ProductTypeService productTypeService;

    public AdminProductTypeController(ProductTypeService productTypeService) {
        this.productTypeService = productTypeService;
    }

    @Override
    public ResponseEntity<ProductTypeResponse> createProductType(ProductTypeRequest productTypeRequest) {
        UUID productTypeId = productTypeService.createProductType(productTypeRequest);

        ProductTypeResponse response = new ProductTypeResponse()
                .message("Product type created successfully")
                .productTypeId(productTypeId);

        return ResponseEntity.status(201).body(response);
    }

    @Override
    public ResponseEntity<ProductTypeDetailsResponse> getProductTypeDetails(UUID productTypeId) {
        ProductTypeDetailsResponse response = productTypeService.getProductTypeDetails(productTypeId);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<GetProductTypes200Response> getProductTypes() {
        List<ProductTypeItemResponse> productTypes = productTypeService.getProductTypes();
        GetProductTypes200Response response = new GetProductTypes200Response();
        response.setProductTypes(productTypes);
        return  ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity addAttributesToProductType(AddAttributesRequest addAttributesRequest) {
        productTypeService.addAttributesToProductType(addAttributesRequest);

        UUID productTypeId = UUID.fromString(addAttributesRequest.getProductTypeID());
        // Fetch updated product type details
        ProductTypeDetailsResponse updatedProductType = productTypeService.getProductTypeDetails(productTypeId);

        return ResponseEntity.ok(updatedProductType);
    }

    @Override
    public ResponseEntity addNotAllowedCombinations(NotAllowedCombinationsRequest notAllowedCombinationsRequest) {
        productTypeService.addNotAllowedCombinations(notAllowedCombinationsRequest);

        AddNotAllowedCombinations200Response response = new AddNotAllowedCombinations200Response();
        response.setMessage("Not-allowed combinations added successfully.");
        return ResponseEntity.ok(response);
    }

}