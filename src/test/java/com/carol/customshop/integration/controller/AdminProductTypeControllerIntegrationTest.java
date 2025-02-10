package com.carol.customshop.integration.controller;

import com.carol.customshop.dto.AddAttributesRequest;
import com.carol.customshop.dto.ProductTypeRequest;
import com.carol.customshop.dto.AttributeRequest;
import com.carol.customshop.dto.ProductTypeRequestConfig;
import com.carol.customshop.entity.ProductType;
import com.carol.customshop.entity.ProductTypeConfig;
import com.carol.customshop.repository.ProductTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
@AutoConfigureMockMvc
class AdminProductTypeControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductTypeRepository productTypeRepository;

    private String fullyCustomisableProductTypeId;
    private String notCustomisableProductTypeId;

    @BeforeEach
    void setUp() {
        // Create Fully Customisable Product Type
        ProductType fullyCustomisableProductType = new ProductType();
        fullyCustomisableProductType.setName("Bicycle");
        fullyCustomisableProductType.setConfig(new ProductTypeConfig("fully_customisable"));
        fullyCustomisableProductType = productTypeRepository.save(fullyCustomisableProductType);
        fullyCustomisableProductTypeId = String.valueOf(fullyCustomisableProductType.getId());

        // Create Not Customisable Product Type
        ProductType notCustomisableProductType = new ProductType();
        notCustomisableProductType.setName("Snowboard");
        notCustomisableProductType.setConfig(new ProductTypeConfig("not_customisable"));
        notCustomisableProductType = productTypeRepository.save(notCustomisableProductType);
        notCustomisableProductTypeId = String.valueOf(notCustomisableProductType.getId());
    }

    @Test
    void shouldCreateProductTypeSuccessfully() throws Exception {
        // Given: A product type request that includes `name` and an embedded config with `customisation`.
        ProductTypeRequestConfig config = new ProductTypeRequestConfig("fully_customisable");
        ProductTypeRequest request = new ProductTypeRequest("Bicycle", config);

        // When: Sending a POST request to create a ProductType
        mockMvc.perform(post("/admin/product-types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                // Then: Expect 201 and a success response
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Product type created successfully"))
                .andExpect(jsonPath("$.productTypeId").isString());
    }

   @Test
    void shouldAddAttributesToFullyCustomisableProductType() throws Exception {
        AddAttributesRequest addRequest = new AddAttributesRequest(
                fullyCustomisableProductTypeId,
                List.of(
                        new AttributeRequest("Frame Finish", List.of("Matte", "Shiny")),
                        new AttributeRequest("Wheels", List.of("Road Wheels", "Cruiser Wheels"))
                )
        );

        mockMvc.perform(post("/admin/product-types/attributes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Attributes added successfully"));
    }

    @Test
    void shouldFailToAddAttributesIfNotFullyCustomisable() throws Exception {
        AddAttributesRequest addRequest = new AddAttributesRequest(
                notCustomisableProductTypeId,
                List.of(
                        new AttributeRequest("Some Attr", List.of("Value1", "Value2"))
                )
        );

        mockMvc.perform(post("/admin/product-types/attributes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Cannot add attributes to a non-fully-customisable product type."));
    }
}
