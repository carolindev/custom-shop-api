package com.carol.customshop.controller;

import com.carol.customshop.api.AdminProductTypesApi;
import com.carol.customshop.dto.*;
import com.carol.customshop.service.ProductTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity addAttributesToProductType(AddAttributesRequest addAttributesRequest) {
        try {
            productTypeService.addAttributesToProductType(addAttributesRequest);

            AddAttributesToProductType200Response response = new AddAttributesToProductType200Response();
            response.setMessage("Attributes added successfully");

            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            // Handle case where attributes cannot be added
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setError(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @Override
    public ResponseEntity addNotAllowedCombinations(NotAllowedCombinationsRequest notAllowedCombinationsRequest) {
        try {
            productTypeService.addNotAllowedCombinations(notAllowedCombinationsRequest);

            // Build the success response
            AddNotAllowedCombinations200Response response = new AddNotAllowedCombinations200Response();
            response.setMessage("Not-allowed combinations added successfully.");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            // Handle validation exceptions (e.g., invalid request format, not enough combinations)
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setError(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (RuntimeException e) {
            // Handle unexpected internal server errors
            ErrorResponse errorResponse = new ErrorResponse();
            errorResponse.setError("An unexpected error occurred.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

}