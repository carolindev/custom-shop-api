package com.carol.customshop.service;

import com.carol.customshop.dto.AttributeRequest;
import com.carol.customshop.service.interfaces.IProductTypeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("not_customisableProductTypeService")
public class NotCustomisableProductTypeService implements IProductTypeService {

    @Override
    public boolean addAttributesToProductType(String productTypeID, List<AttributeRequest> attributes) {
        throw new IllegalStateException("Cannot add attributes to a non-fully-customisable product type.");
    }
}