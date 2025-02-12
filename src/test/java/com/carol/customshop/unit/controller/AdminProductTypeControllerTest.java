package com.carol.customshop.unit.controller;

import com.carol.customshop.controller.AdminProductTypeController;
import com.carol.customshop.dto.*;
import com.carol.customshop.service.ProductTypeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminProductTypeControllerTest {

    @InjectMocks
    private AdminProductTypeController productTypeController;

    @Mock
    private ProductTypeService productTypeService;

    private String fullyCustomizableProductTypeId;
    private String notCustomizableProductTypeId;

    /**
     * Setup method to create fully customizable and non-customizable product types before each test.
     */
    @BeforeEach
    void setUp(TestInfo testInfo) {
        if (testInfo.getTags().contains("needFullyCustomizable")) {
            // Fully customizable product type
            ProductTypeRequestConfig fullyCustomizableConfig = new ProductTypeRequestConfig("fully_customizable");
            ProductTypeRequest fullyCustomizableRequest = new ProductTypeRequest("Bicycle", fullyCustomizableConfig);

            UUID fullyCustomizableUUID = UUID.randomUUID();
            fullyCustomizableProductTypeId = fullyCustomizableUUID.toString();
            lenient().when(productTypeService.createProductType(fullyCustomizableRequest)).thenReturn(fullyCustomizableUUID);
        }
    }

    @Test
    void createProductType_HappyPath() {
        // Given: A Bicycle product type request
        ProductTypeRequestConfig config = new ProductTypeRequestConfig("fully_customizable");
        ProductTypeRequest request = new ProductTypeRequest("Bicycle", config);

        UUID mockProductTypeId = UUID.randomUUID();
        when(productTypeService.createProductType(request)).thenReturn(mockProductTypeId);

        // When: calling the controller method directly
        ResponseEntity<ProductTypeResponse> response = productTypeController.createProductType(request);

        // Then: verify response
        assertEquals(201, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("Product type created successfully", response.getBody().getMessage());
        assertEquals(mockProductTypeId, response.getBody().getProductTypeId());

        // Verify that the service was called exactly once with the correct request
        verify(productTypeService, times(1)).createProductType(request);
    }

    @Test
    @Tag("needFullyCustomizable")
    void shouldAddAttributesToFullyCustomizableProductType() {
        // Given: A fully customizable product type
        AddAttributesRequest request = new AddAttributesRequest(
                fullyCustomizableProductTypeId,
                List.of(
                        new AttributeRequest("Frame Finish", List.of("Matte", "Shiny")),
                        new AttributeRequest("Wheels", List.of("Road Wheels", "Cruiser Wheels"))
                )
        );

        // Mock service response
        when(productTypeService.addAttributesToProductType(request)).thenReturn(true);

        // When: calling the controller method
        ResponseEntity<ProductTypeDetailsResponse> response = productTypeController.addAttributesToProductType(request);

        // Then: verify response
        assertEquals(200, response.getStatusCodeValue());

        // Verify that the service was called exactly once
        verify(productTypeService, times(1)).addAttributesToProductType(request);
    }
}
