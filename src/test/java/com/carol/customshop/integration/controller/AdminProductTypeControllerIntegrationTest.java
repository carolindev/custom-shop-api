package com.carol.customshop.integration.controller;

import com.carol.customshop.dto.*;
import com.carol.customshop.entity.ProductType;
import com.carol.customshop.entity.ProductTypeAttribute;
import com.carol.customshop.entity.ProductTypeAttributeOption;
import com.carol.customshop.entity.ProductTypeConfig;
import com.carol.customshop.repository.ProductTypeAttributeRepository;
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

import java.util.ArrayList;
import java.util.List;

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

    @Autowired
    private ProductTypeAttributeRepository productTypeAttributeRepository;

    private String fullyCustomizableProductTypeId;
    private ProductType fullyCustomizableProductType;
    private String notCustomizableProductTypeId;

    @BeforeEach
    void setUp() {
        // Create Fully Customizable Product Type
        fullyCustomizableProductType = new ProductType();
        fullyCustomizableProductType.setName("Bicycle");
        fullyCustomizableProductType.setConfig(new ProductTypeConfig("fully_customizable"));
        fullyCustomizableProductType = productTypeRepository.save(fullyCustomizableProductType);
        fullyCustomizableProductTypeId = String.valueOf(fullyCustomizableProductType.getId());

        // Create Not Customizable Product Type
        ProductType notCustomizableProductType = new ProductType();
        notCustomizableProductType.setName("Snowboard");
        notCustomizableProductType.setConfig(new ProductTypeConfig("not_customizable"));
        notCustomizableProductType = productTypeRepository.save(notCustomizableProductType);
        notCustomizableProductTypeId = String.valueOf(notCustomizableProductType.getId());

        // Add Attributes with Possible Options
        fullyCustomizableProductType = addAttributesWithOptions(fullyCustomizableProductType);
    }

    private ProductType addAttributesWithOptions(ProductType productType) {
        List<ProductTypeAttribute> attributes = new ArrayList<>();

        ProductTypeAttribute frameFinish = new ProductTypeAttribute();
        frameFinish.setProductType(productType);
        frameFinish.setAttributeName("Frame Finish");

        ProductTypeAttributeOption matte = new ProductTypeAttributeOption();
        matte.setName("Matte");
        matte.setAttribute(frameFinish);

        ProductTypeAttributeOption shiny = new ProductTypeAttributeOption();
        shiny.setName("Shiny");
        shiny.setAttribute(frameFinish);

        frameFinish.setOptions(List.of(matte, shiny));
        attributes.add(frameFinish);

        ProductTypeAttribute wheels = new ProductTypeAttribute();
        wheels.setProductType(productType);
        wheels.setAttributeName("Wheels");

        ProductTypeAttributeOption roadWheels = new ProductTypeAttributeOption();
        roadWheels.setName("Road Wheels");
        roadWheels.setAttribute(wheels);

        ProductTypeAttributeOption cruiserWheels = new ProductTypeAttributeOption();
        cruiserWheels.setName("Cruiser Wheels");
        cruiserWheels.setAttribute(wheels);

        wheels.setOptions(List.of(roadWheels, cruiserWheels));
        attributes.add(wheels);

        attributes = productTypeAttributeRepository.saveAll(attributes);

        productType.setAttributes(attributes);
        return productTypeRepository.save(productType);
    }

    @Test
    void shouldCreateProductTypeSuccessfully() throws Exception {
        // Given: A product type request that includes `name` and an embedded config with `customisation`.
        ProductTypeRequestConfig config = new ProductTypeRequestConfig("fully_customizable");
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
    void shouldAddAttributesToFullyCustomizableProductType() throws Exception {
        AddAttributesRequest addRequest = new AddAttributesRequest(
                fullyCustomizableProductTypeId,
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
    void shouldFailToAddAttributesIfNotFullyCustomizable() throws Exception {
        AddAttributesRequest addRequest = new AddAttributesRequest(
                notCustomizableProductTypeId,
                List.of(
                        new AttributeRequest("Some Attr", List.of("Value1", "Value2"))
                )
        );

        mockMvc.perform(post("/admin/product-types/attributes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Cannot add attributes to a non-fully-customizable product type."));
    }

    @Test
    void shouldAddNotAllowedCombinationsSuccessfully() throws Exception {


        Long atId1 = fullyCustomizableProductType.getAttributes().get(0).getId();
        Long opId1 = fullyCustomizableProductType.getAttributes().get(0).getOptions().get(0).getId();

        Long atId2 = fullyCustomizableProductType.getAttributes().get(1).getId();
        Long opId2 = fullyCustomizableProductType.getAttributes().get(1).getOptions().get(0).getId();


        // Given: A valid request with at least two attribute-options in each combination
        NotAllowedCombinationsRequest request = new NotAllowedCombinationsRequest(
                fullyCustomizableProductTypeId, // Ensure UUID is correctly formatted
                List.of(
                        List.of(
                                new NotAllowedCombinationItem(atId1, opId1),
                                new NotAllowedCombinationItem(atId2, opId2)
                        )/*,
                        List.of(
                                new NotAllowedCombinationItem(103L, 3003L),
                                new NotAllowedCombinationItem(104L, 4004L),
                                new NotAllowedCombinationItem(105L, 5005L)
                        )*/
                )
        );

        // When: Sending a POST request
        mockMvc.perform(post("/admin/product-types/not-allowed-combinations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                // Then: Expect success response
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Not-allowed combinations added successfully."));
    }
}
