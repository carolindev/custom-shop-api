package com.carol.customshop.service;

import com.carol.customshop.dto.*;
import com.carol.customshop.entity.*;
import com.carol.customshop.entity.NotAllowedCombination;
import com.carol.customshop.repository.NotAllowedCombinationRepository;
import com.carol.customshop.repository.ProductTypeAttributeOptionRepository;
import com.carol.customshop.repository.ProductTypeAttributeRepository;
import com.carol.customshop.repository.ProductTypeRepository;
import com.carol.customshop.service.interfaces.IProductTypeService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service("fully_customizableProductTypeService")
public class CustomizableProductTypeService implements IProductTypeService {

    private final ProductTypeRepository productTypeRepository;

    private final NotAllowedCombinationRepository notAllowedCombinationRepository;

    private final ProductTypeAttributeRepository productTypeAttributeRepository;

    private final ProductTypeAttributeOptionRepository productTypeAttributeOptionRepository;

    // Use constructor injection
    public CustomizableProductTypeService(
            ProductTypeRepository productTypeRepository,
            NotAllowedCombinationRepository notAllowedCombinationRepository,
            ProductTypeAttributeRepository productTypeAttributeRepository,
            ProductTypeAttributeOptionRepository productTypeAttributeOptionRepository
    ) {
        this.productTypeRepository = productTypeRepository;
        this.notAllowedCombinationRepository = notAllowedCombinationRepository;
        this.productTypeAttributeRepository = productTypeAttributeRepository;
        this.productTypeAttributeOptionRepository = productTypeAttributeOptionRepository;
    }
    @Override
    public boolean addAttributesToProductType(String productTypeID, List<AttributeRequest> attributes) {
        UUID productTypeUuid = UUID.fromString(productTypeID);

        ProductType productType = productTypeRepository.findById(productTypeUuid)
                .orElseThrow(() -> new EntityNotFoundException("Product Type not found with ID: " + productTypeID));

        // Convert AttributeRequests into ProductTypeAttribute entities
        List<ProductTypeAttribute> newAttributes = attributes.stream().map(attrReq -> {
            ProductTypeAttribute attribute = new ProductTypeAttribute();
            attribute.setProductType(productType);
            attribute.setAttributeName(attrReq.getAttributeName());

            // Convert possibleOptions into ProductTypeAttributeOption entities
            List<ProductTypeAttributeOption> options = attrReq.getPossibleOptions().stream()
                    .map(optionValue -> {
                        ProductTypeAttributeOption option = new ProductTypeAttributeOption();
                        option.setName(optionValue);
                        option.setAttribute(attribute);
                        return option;
                    })
                    .collect(Collectors.toList());

            // Add options to the attribute
            attribute.setOptions(options);
            return attribute;
        }).collect(Collectors.toList());

        productType.getAttributes().addAll(newAttributes);
        productTypeRepository.save(productType);

        return true;
    }

    @Override
    @Transactional
    public void addNotAllowedCombinations(
            String productTypeId, List<List<NotAllowedCombinationItem>> notAllowedCombinations) {

        ProductType productType = productTypeRepository.findById(UUID.fromString(productTypeId))
                .orElseThrow(() -> new IllegalArgumentException("Product type not found."));

        if (notAllowedCombinations == null || notAllowedCombinations.isEmpty()) {
            throw new IllegalArgumentException("There must be at least one not-allowed combination provided.");
        }

        // Ensure each sub-list has at least 2 items.
        for (List<NotAllowedCombinationItem> combination : notAllowedCombinations) {
            if (combination == null || combination.size() < 2) {
                throw new IllegalArgumentException(
                        "Each not-allowed combination must have at least two attribute-option pairs."
                );
            }
        }

        List<NotAllowedCombination> combinations = notAllowedCombinations.stream()
                .map(combination -> {

                    NotAllowedCombination parentCombination = new NotAllowedCombination();
                    parentCombination.setProductType(productType);

                    List<NotAllowedCombinationOption> options = combination.stream()
                            .map(option -> {
                                // Fetch and validate attribute
                                ProductTypeAttribute attribute =
                                        productTypeAttributeRepository.findById(option.getAttributeId())
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                "Attribute not found with ID: " + option.getAttributeId()));

                                // Ensure the attribute belongs to the same product type
                                if (!attribute.getProductType().getId().equals(productType.getId())) {
                                    throw new IllegalArgumentException(
                                            "Attribute with ID " + option.getAttributeId() +
                                                    " does not belong to the specified Product Type.");
                                }

                                // Fetch and validate attribute option
                                ProductTypeAttributeOption attributeOption =
                                        productTypeAttributeOptionRepository.findById(option.getAttributeOptionId())
                                        .orElseThrow(() -> new IllegalArgumentException(
                                                "Attribute option not found with ID " + option.getAttributeOptionId()));

                                // Ensure the attribute option belongs to the correct attribute and product type
                                if (!attributeOption.getAttribute().getId().equals(attribute.getId())) {
                                    throw new IllegalArgumentException(
                                            "Option with ID "
                                                    + option.getAttributeOptionId()
                                                    + " does not belong to the Attribute with ID "
                                                    + option.getAttributeId());
                                }

                                // Create NotAllowedCombinationOption entity
                                NotAllowedCombinationOption notAllowedCombinationOption =
                                        new NotAllowedCombinationOption();
                                notAllowedCombinationOption.setAttribute(attribute);
                                notAllowedCombinationOption.setAttributeOption(attributeOption);
                                notAllowedCombinationOption.setNotAllowedCombination(parentCombination);

                                return notAllowedCombinationOption;
                            })
                            .collect(Collectors.toList());

                    parentCombination.setOptions(options);
                    return notAllowedCombinationRepository.save(parentCombination);

                })
                .collect(Collectors.toList());

        productType.getNotAllowedCombinations().addAll(combinations);
        productTypeRepository.save(productType);
    }

    @Override
    public ProductTypeDetailsResponse getAdditionalProductTypeDetails(UUID productTypeId) {
        ProductType productType = productTypeRepository.findById(productTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Product type not found."));

        // Fetch Attributes and Options
        List<ProductAttribute> attributes = productType.getAttributes().stream()
                .map(attribute -> {
                    ProductAttribute productAttribute = new ProductAttribute();
                    productAttribute.setId(attribute.getId());
                    productAttribute.setName(attribute.getAttributeName());

                    List<AttributeOption> options = attribute.getOptions().stream()
                            .map(option -> {
                                AttributeOption optionResponse = new AttributeOption();
                                optionResponse.setId(option.getId());
                                optionResponse.setName(option.getName());
                                return optionResponse;
                            })
                            .collect(Collectors.toList());

                    productAttribute.setOptions(options);
                    return productAttribute;
                })
                .collect(Collectors.toList());

        // Fetch Not-Allowed Combinations
        List<NotAllowedCombinationR> notAllowedCombinations = productType.getNotAllowedCombinations().stream()
                .map(combination -> {
                    NotAllowedCombinationR notAllowedCombination = new NotAllowedCombinationR();
                    notAllowedCombination.setCombinationId(combination.getId());

                    List<AttributeOption> options = combination.getOptions().stream()
                            .map(option -> {
                                AttributeOption optionResponse = new AttributeOption();
                                optionResponse.setId(option.getId());
                                optionResponse.setName(option.getAttributeOption().getName());
                                return optionResponse;
                            })
                            .collect(Collectors.toList());

                    notAllowedCombination.setOptions(options);
                    return notAllowedCombination;
                })
                .collect(Collectors.toList());

        ProductTypeDetailsResponse response = new ProductTypeDetailsResponse();
        response.setAttributes(attributes);
        response.setNotAllowedCombinations(notAllowedCombinations);

        return response;
    }

}
