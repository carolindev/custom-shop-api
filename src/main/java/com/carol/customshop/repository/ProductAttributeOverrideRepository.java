package com.carol.customshop.repository;

import com.carol.customshop.entity.ProductAttributeOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductAttributeOverrideRepository extends JpaRepository<ProductAttributeOverride, Long> {}