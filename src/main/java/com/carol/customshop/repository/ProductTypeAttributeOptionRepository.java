package com.carol.customshop.repository;

import com.carol.customshop.entity.ProductTypeAttributeOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductTypeAttributeOptionRepository extends JpaRepository<ProductTypeAttributeOption, Long> {}