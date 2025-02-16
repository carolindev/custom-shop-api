package com.carol.customshop.repository;

import com.carol.customshop.entity.Product;
import com.carol.customshop.entity.ProductAttributeOverride;
import com.carol.customshop.entity.ProductOptionOverride;
import com.carol.customshop.entity.ProductTypeAttributeOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductOptionOverrideRepository extends JpaRepository<ProductOptionOverride, Long> {
    Optional<ProductOptionOverride> findByProductAndOption(Product product, ProductTypeAttributeOption option);

    List<ProductOptionOverride> findByProductAndActiveFalse(Product product);

    List<ProductOptionOverride> findByProductAndOutOfStockTrue(Product product);
}