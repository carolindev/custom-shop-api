package com.carol.customshop.service;

import com.carol.customshop.service.interfaces.IProductTypeService;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Map;

@Component
public class ProductTypeServiceFactory {

    private final Map<String, IProductTypeService> services;

    public ProductTypeServiceFactory(Map<String, IProductTypeService> services) {
        // Spring automatically injects a map of all beans that implement IProductService,
        this.services = services;
    }

    public IProductTypeService getService(String customisation) {
        String beanName = customisation + "ProductTypeService";
        IProductTypeService service = services.get(beanName);
        if (service == null) {
            throw new RuntimeException("No IProductTypeService bean found for: " + beanName);
        }
        return service;
    }
}