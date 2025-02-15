package com.carol.customshop.service;

import com.carol.customshop.dto.*;
import com.carol.customshop.entity.*;
import com.carol.customshop.repository.NotAllowedCombinationRepository;
import com.carol.customshop.repository.ProductTypeAttributeOptionRepository;
import com.carol.customshop.repository.ProductTypeAttributeRepository;
import com.carol.customshop.repository.ProductTypeRepository;
import com.carol.customshop.service.interfaces.IProductTypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductTypeService {

    private final ProductTypeRepository productTypeRepository;

    private final ProductTypeAttributeRepository productTypeAttributeRepository;

    private final ProductTypeAttributeOptionRepository productTypeAttributeOptionRepository;

    private final NotAllowedCombinationRepository notAllowedCombinationRepository;

    private final ProductTypeServiceFactory productTypeServiceFactory;

    public ProductTypeService(
            ProductTypeRepository productTypeRepository,
            ProductTypeAttributeRepository productTypeAttributeRepository,
            ProductTypeAttributeOptionRepository productTypeAttributeOptionRepository,
            NotAllowedCombinationRepository notAllowedCombinationRepository,
            ProductTypeServiceFactory productTypeServiceFactory
    ) {
        this.productTypeRepository = productTypeRepository;
        this.productTypeAttributeRepository = productTypeAttributeRepository;
        this.productTypeAttributeOptionRepository = productTypeAttributeOptionRepository;
        this.notAllowedCombinationRepository = notAllowedCombinationRepository;
        this.productTypeServiceFactory = productTypeServiceFactory;
    }

    @Transactional
    public UUID createProductType(ProductTypeRequest request) {
        ProductType productType = new ProductType();
        productType.setName(request.getName());

        ProductTypeConfig config = new ProductTypeConfig();
        config.setCustomisation(request.getConfig().getCustomisation());
        productType.setConfig(config);

        productTypeRepository.save(productType);

        return productType.getId();
    }

    public boolean addAttributesToProductType(AddAttributesRequest addAttributesRequest) {
        IProductTypeService specificProductTypeService = getProductTypeService(addAttributesRequest.getProductTypeID());
        // Delegate the operation to the appropriate Product Type service
        return specificProductTypeService.addAttributesToProductType(addAttributesRequest.getProductTypeID(),
                addAttributesRequest.getAttributes());
    }

    @Transactional
    public void addNotAllowedCombinations(NotAllowedCombinationsRequest request) {
        IProductTypeService specificProductTypeService = getProductTypeService(request.getProductTypeId());
        // Delegate the operation to the appropriate Product Type service
        specificProductTypeService.addNotAllowedCombinations(
                request.getProductTypeId(),
                request.getNotAllowedCombinations()
        );
    }

    public List<ProductTypeItemResponse> getProductTypes() {
        return productTypeRepository.findAll().stream()
                .map(productType -> {
                    ProductTypeItemResponse response = new ProductTypeItemResponse();
                    response.setId(productType.getId());
                    response.setName(productType.getName());

                    ProductTypeItemResponseConfig config = new ProductTypeItemResponseConfig();
                    config.setCustomisation(productType.getConfig().getCustomisation());
                    response.setConfig(config);

                    return response;
                })
                .collect(Collectors.toList());
    }

    public ProductTypeDetailsResponse getProductTypeDetails(UUID productTypeId) {
        ProductType productType = getProductTypeById(productTypeId.toString());
        ProductTypeDetailsResponse response = buildBasicProductTypeDetails(productType);

        IProductTypeService specificProductTypeService = getProductTypeService(productTypeId.toString());
        ProductTypeDetailsResponse additionalDetails =
                specificProductTypeService.getAdditionalProductTypeDetails(productTypeId);

        response.setAttributes(additionalDetails.getAttributes());
        response.setNotAllowedCombinations(additionalDetails.getNotAllowedCombinations());

        return response;
    }

    public ProductType getProductTypeById(String productTypeId) {
        return productTypeRepository.findById(UUID.fromString(productTypeId))
                .orElseThrow(() -> new IllegalArgumentException("Product type not found."));
    }

    // Get the Correct Service Based on Customization
    private IProductTypeService getProductTypeService(String productTypeId) {
        ProductType productType = getProductTypeById(productTypeId);
        String customisation = productType.getConfig().getCustomisation();
        return productTypeServiceFactory.getService(customisation);
    }

    private ProductTypeDetailsResponse buildBasicProductTypeDetails(ProductType productType) {
        ProductTypeDetailsResponse response = new ProductTypeDetailsResponse();
        response.setId(productType.getId());
        response.setName(productType.getName());

        ProductTypeDetailsResponseConfig config = new ProductTypeDetailsResponseConfig();
        config.setCustomization(productType.getConfig().getCustomisation());
        response.setConfig(config);

        return response;
    }

    public ProductTypeAttribute getProductTypeAttributeById(Long attributeId) {
        return productTypeAttributeRepository.findById(attributeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Product Type Attribute not found with ID: " + attributeId));
    }

    public ProductTypeAttributeOption getProductTypeAttributeOptionById(Long optionId) {
        return productTypeAttributeOptionRepository.findById(optionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Product Type Attribute Option not found with ID: " + optionId));
    }

    public NotAllowedCombination getNotAllowedCombinationById(Long combinationId) {
        return notAllowedCombinationRepository.findById(combinationId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Not-allowed combination not found with ID: " + combinationId));
    }
}