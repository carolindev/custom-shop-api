package com.carol.customshop.service.interfaces;


import com.carol.customshop.dto.AttributeRequest;

import java.util.List;

public interface IProductTypeService {
    boolean addAttributesToProductType(String productTypeID, List<AttributeRequest> attributes);
}