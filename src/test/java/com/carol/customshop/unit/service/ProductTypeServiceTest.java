package com.carol.customshop.unit.service;

import com.carol.customshop.dto.NotAllowedCombinationItem;
import com.carol.customshop.dto.NotAllowedCombinationsRequest;
import com.carol.customshop.entity.ProductType;
import com.carol.customshop.entity.ProductTypeConfig;
import com.carol.customshop.repository.ProductTypeRepository;
import com.carol.customshop.service.CustomizableProductTypeService;
import com.carol.customshop.service.ProductTypeService;
import com.carol.customshop.service.ProductTypeServiceFactory;
import com.carol.customshop.service.interfaces.IProductTypeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductTypeServiceTest {

    @InjectMocks
    private ProductTypeService productTypeService;

    @Mock
    private ProductTypeRepository productTypeRepository;

    @Mock
    private ProductTypeServiceFactory productTypeServiceFactory;

    private UUID fullyCustomizableProductTypeId;
    private ProductType fullyCustomizableProductType;

    @BeforeEach
    void setUp() {
        fullyCustomizableProductTypeId = UUID.randomUUID();

        // Mock a fully customizable product type
        fullyCustomizableProductType = new ProductType();
        fullyCustomizableProductType.setId(fullyCustomizableProductTypeId);

        ProductTypeConfig config = new ProductTypeConfig();
        config.setCustomisation("fully_customizable"); // Ensure it's customizable
        fullyCustomizableProductType.setConfig(config);

        when(productTypeRepository.findById(fullyCustomizableProductTypeId))
                .thenReturn(Optional.of(fullyCustomizableProductType));

        IProductTypeService CustomizableProductTypeService = new CustomizableProductTypeService(productTypeRepository);

        when(productTypeServiceFactory.getService(anyString()))
                .thenReturn(CustomizableProductTypeService);
    }

    @Test
    void shouldSaveNotAllowedCombinationsSuccessfully() {
        // Given: A valid request with at least two attribute-options in each combination
        NotAllowedCombinationsRequest request = new NotAllowedCombinationsRequest(
                fullyCustomizableProductTypeId.toString(), // Ensure UUID is converted to string
                List.of(
                        List.of(
                                new NotAllowedCombinationItem(101L, 1001L),
                                new NotAllowedCombinationItem(102L, 2002L)
                        ),
                        List.of(
                                new NotAllowedCombinationItem(103L, 3003L),
                                new NotAllowedCombinationItem(104L, 4004L),
                                new NotAllowedCombinationItem(105L, 5005L)
                        )
                )
        );

        // When: Adding not-allowed combinations
        productTypeService.addNotAllowedCombinations(request);

        // Then: Verify repository interaction (data is saved)
        verify(productTypeRepository, times(1)).save(fullyCustomizableProductType);

        // Ensure not-allowed combinations were added
        assertFalse(fullyCustomizableProductType.getNotAllowedCombinations().isEmpty(),
                "Not-allowed combinations should be added");
    }

    @Test
    void shouldThrowExceptionWhenCombinationHasLessThanTwoOptions() {
        // Given: A request with an invalid combination (only one attribute-option)
        NotAllowedCombinationsRequest request = new NotAllowedCombinationsRequest(
                fullyCustomizableProductTypeId.toString(),
                List.of(
                        List.of( // INVALID: Only one element
                                new NotAllowedCombinationItem(101L, 1001L)
                        )
                )
        );

        // When & Then: Expect IllegalArgumentException to be thrown
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            productTypeService.addNotAllowedCombinations(request);
        });

        assertEquals("Each not-allowed combination must have at least two attribute-option pairs.", exception.getMessage());
    }
}
