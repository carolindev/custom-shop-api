package com.carol.customshop.service;

import com.carol.customshop.dto.AddAttributesRequest;
import com.carol.customshop.dto.NotAllowedCombinationsRequest;
import com.carol.customshop.dto.ProductTypeRequest;
import com.carol.customshop.entity.ProductType;
import com.carol.customshop.entity.ProductTypeConfig;
import com.carol.customshop.repository.ProductTypeRepository;
import com.carol.customshop.service.interfaces.IProductTypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ProductTypeService {

    private final ProductTypeRepository productTypeRepository;

    private final ProductTypeServiceFactory productTypeServiceFactory;

    public ProductTypeService(
            ProductTypeRepository productTypeRepository, ProductTypeServiceFactory productTypeServiceFactory) {
        this.productTypeRepository = productTypeRepository;
        this.productTypeServiceFactory = productTypeServiceFactory;
    }

    @Transactional
    public UUID createProductType(ProductTypeRequest request) {
        ProductType productType = new ProductType();
        productType.setName(request.getName());

        ProductTypeConfig config = new ProductTypeConfig();
        config.setCustomisation(request.getConfig().getCustomisation());
        productType.setConfig(config);

        productType = productTypeRepository.save(productType);

        return productType.getId();
    }

    public boolean addAttributesToProductType(AddAttributesRequest addAttributesRequest) {

        ProductType pType = productTypeRepository.findById(UUID.fromString(addAttributesRequest.getProductTypeID()))
                .orElseThrow(() -> new RuntimeException(
                        "No ProductType found with id=" + addAttributesRequest.getProductTypeID()));

        // Get the customization type from the Product Type configuration
        String customisation = pType.getConfig().getCustomisation();

        // Retrieve the correct service implementation based on the customization product type
        IProductTypeService specificProductTypeService = productTypeServiceFactory.getService(customisation);

        // Delegate the operation to the appropriate Product Type service
        return specificProductTypeService.addAttributesToProductType(addAttributesRequest.getProductTypeID(),
                addAttributesRequest.getAttributes());
    }

    @Transactional
    public void addNotAllowedCombinations(NotAllowedCombinationsRequest request) {
        ProductType productType = productTypeRepository.findById(UUID.fromString(request.getProductTypeId()))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Product type not found.")
                );

        String customisation = productType.getConfig().getCustomisation();

        // Retrieve the correct service implementation based on the customization strategy
        IProductTypeService specificProductTypeService = productTypeServiceFactory.getService(customisation);

        // Delegate the operation to the appropriate Product Type service
        specificProductTypeService.addNotAllowedCombinations(
                request.getProductTypeId(),
                request.getNotAllowedCombinations()
        );
    }
}