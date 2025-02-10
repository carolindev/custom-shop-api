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

    private String fullyCustomisableProductTypeId;
    private String notCustomisableProductTypeId;

    /**
     * Setup method to create fully customisable and non-customisable product types before each test.
     */
    @BeforeEach
    void setUp(TestInfo testInfo) {
        if (testInfo.getDisplayName().equals("shouldAddAttributesToFullyCustomisableProductType")) {
            // Fully customisable product type
            ProductTypeRequestConfig fullyCustomisableConfig = new ProductTypeRequestConfig("fully_customisable");
            ProductTypeRequest fullyCustomisableRequest = new ProductTypeRequest("Bicycle", fullyCustomisableConfig);

            UUID fullyCustomisableUUID = UUID.randomUUID();
            fullyCustomisableProductTypeId = fullyCustomisableUUID.toString();
            when(productTypeService.createProductType(fullyCustomisableRequest)).thenReturn(fullyCustomisableUUID);
        }

        if (testInfo.getDisplayName().equals("shouldFailToAddAttributesIfNotFullyCustomisable")) {
            // Not customisable product type
            ProductTypeRequestConfig notCustomisableConfig = new ProductTypeRequestConfig("not_customisable");
            ProductTypeRequest notCustomisableRequest = new ProductTypeRequest("Snowboard", notCustomisableConfig);

            UUID notCustomisableUUID = UUID.randomUUID();
            notCustomisableProductTypeId = notCustomisableUUID.toString();
            when(productTypeService.createProductType(notCustomisableRequest)).thenReturn(notCustomisableUUID);
        }
    }

    @Test
    void createProductType_HappyPath() {
        // Given: A Bicycle product type request
        ProductTypeRequestConfig config = new ProductTypeRequestConfig("fully_customisable");
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
    @Tag("needsSetup")
    void shouldAddAttributesToFullyCustomisableProductType() {
        // Given: A fully customisable product type
        AddAttributesRequest request = new AddAttributesRequest(
                fullyCustomisableProductTypeId,
                List.of(
                        new AttributeRequest("Frame Finish", List.of("Matte", "Shiny")),
                        new AttributeRequest("Wheels", List.of("Road Wheels", "Cruiser Wheels"))
                )
        );

        // Mock service response
        when(productTypeService.addAttributesToProductType(request)).thenReturn(true);

        // When: calling the controller method
        ResponseEntity<AddAttributesToProductType200Response> response = productTypeController.addAttributesToProductType(request);

        // Then: verify response
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("Attributes added successfully", response.getBody().getMessage());

        // Verify that the service was called exactly once
        verify(productTypeService, times(1)).addAttributesToProductType(request);
    }

    @Test
    @Tag("needsSetup")
    void shouldFailToAddAttributesIfNotFullyCustomisable() {
        // Given: A non-customisable product type
        AddAttributesRequest request = new AddAttributesRequest(
                notCustomisableProductTypeId,
                List.of(
                        new AttributeRequest("Some Attr", List.of("Value1", "Value2"))
                )
        );

        // Mock service to throw an exception when attempting to add attributes
        doThrow(new IllegalStateException("Cannot add attributes to a non-fully-customisable product type."))
                .when(productTypeService).addAttributesToProductType(request);

        // When: calling the controller method
        ResponseEntity<ErrorResponse> response = productTypeController.addAttributesToProductType(request);

        // Then: verify response
        assertEquals(400, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("Cannot add attributes to a non-fully-customisable product type.", response.getBody().getError());

        // Verify that the service method was called exactly once
        verify(productTypeService, times(1)).addAttributesToProductType(request);
    }
}
