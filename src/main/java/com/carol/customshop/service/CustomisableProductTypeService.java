package com.carol.customshop.service;

import com.carol.customshop.dto.AttributeRequest;
import com.carol.customshop.entity.ProductType;
import com.carol.customshop.entity.ProductTypeAttributeOption;
import com.carol.customshop.repository.ProductTypeRepository;
import com.carol.customshop.entity.ProductTypeAttribute;
import com.carol.customshop.service.interfaces.IProductTypeService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service("fully_customisableProductTypeService")
public class CustomisableProductTypeService implements IProductTypeService {

    @Autowired
    private ProductTypeRepository productTypeRepository;
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
}
