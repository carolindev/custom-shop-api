package com.carol.customshop.repository;

import com.carol.customshop.entity.ProductNACombinationOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductNACombinationOverrideRepository extends JpaRepository<ProductNACombinationOverride, Long> {}