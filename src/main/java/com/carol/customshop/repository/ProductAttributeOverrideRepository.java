package com.carol.customshop.repository;

import com.carol.customshop.entity.Product;
import com.carol.customshop.entity.ProductAttributeOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductAttributeOverrideRepository extends JpaRepository<ProductAttributeOverride, Long> {
    List<ProductAttributeOverride> findByProductAndActiveFalse(Product product);
}