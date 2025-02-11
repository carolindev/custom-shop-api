package com.carol.customshop.service.interfaces;


import com.carol.customshop.dto.AttributeRequest;
import com.carol.customshop.dto.NotAllowedCombinationItem;
import com.carol.customshop.dto.ProductTypeDetailsResponse;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.UUID;

public interface IProductTypeService {
    boolean addAttributesToProductType(String productTypeID, List<AttributeRequest> attributes);

    @Transactional
    void addNotAllowedCombinations(String productTypeId, List<List<NotAllowedCombinationItem>> notAllowedCombinations);

    ProductTypeDetailsResponse getAdditionalProductTypeDetails(UUID productTypeId);
}