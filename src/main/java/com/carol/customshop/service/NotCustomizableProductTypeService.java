package com.carol.customshop.service;

import com.carol.customshop.dto.AttributeRequest;
import com.carol.customshop.dto.NotAllowedCombinationItem;
import com.carol.customshop.service.interfaces.IProductTypeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("not_customizableProductTypeService")
public class NotCustomizableProductTypeService implements IProductTypeService {

    @Override
    public boolean addAttributesToProductType(String productTypeID, List<AttributeRequest> attributes) {
        throw new IllegalStateException("Cannot add attributes to a non-fully-customizable product type.");
    }

    @Override
    public void addNotAllowedCombinations(
            String productTypeId,
            List<List<NotAllowedCombinationItem>> notAllowedCombinations
    ) {
        throw new IllegalStateException("Cannot add combinations to a non-fully-customizable product type.");
    }
}