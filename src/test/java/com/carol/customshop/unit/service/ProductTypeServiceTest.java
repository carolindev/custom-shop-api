package com.carol.customshop.unit.service;

import com.carol.customshop.dto.NotAllowedCombinationItem;
import com.carol.customshop.dto.NotAllowedCombinationsRequest;
import com.carol.customshop.entity.ProductType;
import com.carol.customshop.entity.ProductTypeAttributeOption;
import com.carol.customshop.entity.ProductTypeConfig;
import com.carol.customshop.repository.NotAllowedCombinationRepository;
import com.carol.customshop.repository.ProductTypeAttributeOptionRepository;
import com.carol.customshop.repository.ProductTypeAttributeRepository;
import com.carol.customshop.repository.ProductTypeRepository;
import com.carol.customshop.service.CustomizableProductTypeService;
import com.carol.customshop.service.NotCustomizableProductTypeService;
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
    private ProductTypeAttributeRepository productTypeAttributeRepository;

    @Mock
    private ProductTypeAttributeOptionRepository productTypeAttributeOptionRepository;

    @Mock
    private NotAllowedCombinationRepository notAllowedCombinationRepository;

    @Mock
    private ProductTypeServiceFactory productTypeServiceFactory;

    private UUID fullyCustomizableProductTypeId;
    private ProductType fullyCustomizableProductType;

    @BeforeEach
    void setUp(TestInfo testInfo) {
        String methodName = testInfo.getTestMethod()
                .map(method -> method.getName())
                .orElse("");

        if (methodName.equals("shouldSaveNotAllowedCombinationsSuccessfully")
                || methodName.equals("shouldThrowExceptionWhenCombinationHasLessThanTwoOptions"))
        {
            fullyCustomizableProductTypeId = UUID.randomUUID();

            fullyCustomizableProductType = new ProductType();
            fullyCustomizableProductType.setId(fullyCustomizableProductTypeId);

            ProductTypeConfig config = new ProductTypeConfig();
            config.setCustomisation("fully_customizable");
            fullyCustomizableProductType.setConfig(config);

            when(productTypeRepository.findById(fullyCustomizableProductTypeId))
                    .thenReturn(Optional.of(fullyCustomizableProductType));

            IProductTypeService cService = new CustomizableProductTypeService(
                    productTypeRepository,
                    notAllowedCombinationRepository,
                    productTypeAttributeRepository,
                    productTypeAttributeOptionRepository
            );
            when(productTypeServiceFactory.getService(eq("fully_customizable")))
                    .thenReturn(cService);
        }
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

    @Test
    void shouldThrowExceptionWhenProductTypeDoesNotExist() {
        // Given: A request with a non-existent product type
        UUID invalidProductTypeId = UUID.randomUUID();
        NotAllowedCombinationsRequest request = new NotAllowedCombinationsRequest(
                invalidProductTypeId.toString(),
                List.of(
                        List.of(
                                new NotAllowedCombinationItem(101L, 1001L),
                                new NotAllowedCombinationItem(102L, 2002L)
                        )
                )
        );

        when(productTypeRepository.findById(invalidProductTypeId)).thenReturn(Optional.empty());

        // When & Then: Expect IllegalArgumentException
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            productTypeService.addNotAllowedCombinations(request);
        });

        assertEquals("Product type not found.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenProductTypeIsNotCustomizable() {
        UUID productTypeId = UUID.randomUUID();
        ProductType notCustomizableProductType = new ProductType();
        notCustomizableProductType.setId(productTypeId);

        ProductTypeConfig config = new ProductTypeConfig();
        config.setCustomisation("not_customizable");
        notCustomizableProductType.setConfig(config);

        // Stub the repository to return this productType
        when(productTypeRepository.findById(productTypeId))
                .thenReturn(Optional.of(notCustomizableProductType));

        // Stub the factory to return the *service* for "not_customizable"
        when(productTypeServiceFactory.getService(eq("not_customizable")))
                .thenReturn(new NotCustomizableProductTypeService());

        NotAllowedCombinationsRequest request = new NotAllowedCombinationsRequest(
                productTypeId.toString(),
                List.of(
                        List.of(
                                new NotAllowedCombinationItem(101L, 1001L),
                                new NotAllowedCombinationItem(102L, 2002L)
                        )
                )
        );

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            productTypeService.addNotAllowedCombinations(request);
        });

        assertEquals("Cannot add combinations to a non-fully-customizable product type.",
                exception.getMessage());
    }
}
